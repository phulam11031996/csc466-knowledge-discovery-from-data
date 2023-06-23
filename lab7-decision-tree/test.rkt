#lang typed/racket

(require typed/rackunit)

;; DATA DEFINITIONS
;; Expr
(define-type TyExprC (U numC idC strC binC conC recC lamC begC appC))
(struct numC ([n : Real]) #:transparent)
(struct idC ([s : Symbol]) #:transparent)
(struct strC ([s : String]) #:transparent)
(struct binC ([id : Symbol] [val : TyExprC]) #:transparent)
(struct conC ([if : TyExprC] [then : TyExprC] [else : TyExprC]) #:transparent)
(struct lamC ([args : (Listof Symbol)] [argsT : (Listof Ty)] [body : TyExprC]) #:transparent)
(struct begC ([exprs : (Listof TyExprC)]) #:transparent)
(struct appC ([fun : TyExprC] [args : (Listof TyExprC)]) #:transparent)
(struct recC ([name : Symbol]
              [args : (Listof Symbol)]
              [argsT : (Listof Ty)]
              [rett : Ty]
              [body : TyExprC]
              [use : TyExprC]) #:transparent)

;; ty
(define-type Ty (U numT boolT strT voidT funT numarrayT))
(struct numT () #:transparent)
(struct boolT () #:transparent)
(struct strT () #:transparent)
(struct voidT () #:transparent)
(struct funT ([argst : (Listof Ty)] [rett : Ty]) #:transparent)
(struct numarrayT () #:transparent)

;; bindings
(struct Binding ([name : Symbol] [box : (Boxof Value)]) #:transparent)
(struct TyBinding ([name : Symbol] [ty : Ty]) #:transparent)

;; Value
(define-type Value
  (U Real Boolean String closV Void
    '+ '- '* '/ '<=
    'num-eq? 'str-eq? 'substring
    'makearr 'arr 'aref 'aset 'alen
     'true 'false 'error
     (Vectorof Real)))
(struct closV ([args : (Listof Symbol)] [body : TyExprC] [env : (Listof Binding)]) #:transparent)

;; type env
(define base-tenv
  (list
   (TyBinding '+ (funT (list (numT) (numT)) (numT)))
   (TyBinding '- (funT (list (numT) (numT)) (numT)))
   (TyBinding '* (funT (list (numT) (numT)) (numT)))
   (TyBinding '/ (funT (list (numT) (numT)) (numT)))
   (TyBinding '<= (funT (list (numT) (numT)) (boolT)))
   (TyBinding 'num-eq? (funT (list (numT) (numT)) (boolT)))
   (TyBinding 'str-eq? (funT (list (strT) (strT)) (boolT)))
   (TyBinding 'substring (funT (list (strT) (numT) (numT)) (strT)))
   (TyBinding 'makearr (funT '() (numarrayT)))
   (TyBinding 'arr (funT (list (numT) (numT)) (numarrayT)))
   (TyBinding 'aref (funT (list (numarrayT) (numT)) (numT)))
   (TyBinding 'aset (funT (list (numarrayT) (numT) (numT)) (voidT)))
   (TyBinding 'alen (funT (list (numarrayT)) (numT)))
   (TyBinding 'true (boolT))
   (TyBinding 'false (boolT))))

;; env
(define base-env
  (list
   (Binding '+ (box '+))
   (Binding '- (box '-))
   (Binding '* (box '*)) 
   (Binding '/ (box '/))
   (Binding '<= (box '<=))
   (Binding 'num-eq? (box 'num-eq?))
   (Binding 'str-eq? (box 'str-eq?))
   (Binding 'substring (box 'substring))
   (Binding 'makearr (box 'makearr))
   (Binding 'arr (box 'arr))
   (Binding 'aref (box 'aref))
   (Binding 'aset (box 'aset))
   (Binding 'alen (box 'alen))
   (Binding 'true (box #t))
   (Binding 'false (box #f))
   (Binding 'error (box 'error))))

;; INTERFACE
;; Header: Combines parsing, type-checking, interpretation, and serialization.
(define (top-interp [s : Sexp]) : Value
  (define parsed (parse s))
  (type-check parsed base-tenv)
  (serialize (interp parsed base-env)))

;; Header: Parses an expression.
(define (parse [sexp : Sexp]) : TyExprC
  (match sexp
    ;; Num
    [(? real? n) (numC n)]
    ;; id
    [(? symbol? s)
     (cond
       [(member s '(let rec = if then else : -> begin makearr)) (error 'IKEU "parse: identifier ~s is not allowed" s)]
       [else (idC s)])]
    ;; String
    [(? string? str) (strC str)]
    ;; {id := Expr}
    [(list (? symbol? id) ':= val) (binC id (parse val))]
    ;; {if Expr then Expr else Expr}
    [(list 'if if 'then then 'else else) (conC (parse if) (parse then) (parse else))]
    ;; {let {[ty id = Expr] ...} Expr}
    [(list 'let (list (list tys ids '= exprs) ...) body)
     (local ([define dup-value? (check-duplicates ids)]
             [define listof-symbol? (foldl (lambda (v l) (and (symbol? v) l)) #t ids)])
       (cond
         [(not listof-symbol?) (error 'IKEU "parse: args is not a list of symbols, given ~e" ids)]
         [(equal? dup-value? #f)
          (appC
           (lamC (cast ids (Listof Symbol)) (map parse-type (cast tys (Listof Sexp))) (parse body))
           (map parse (cast exprs (Listof Sexp))))]
         [else (error 'IKEU "parse: duplicates parameters, given ~s" ids)]))]
    ;; {rec {ty id {[ty id] ...} : Expr} Expr}
    [(list 'rec (list (? symbol? rett) (? symbol? name) (list (list argts args) ...) ': body) use)
     (recC name
           (cast args (Listof Symbol))
           (map parse-type (cast argts (Listof Sexp)))
           (parse-type rett)
           (parse body)
           (parse use))]
    ;; {{[ty id] ...} : Expr}
    [(list (list (list tys ids) ...) ': body)
     (local ([define dup-value? (check-duplicates ids)]
             [define listof-symbol? (foldl (lambda (v l) (and (symbol? v) l)) #t ids)])
       (cond
         [(not listof-symbol?) (error 'IKEU "parse: args is not a list of symbols, given ~e" ids)]
         [(equal? dup-value? #f)
          (lamC (cast ids (Listof Symbol)) (map parse-type (cast tys (Listof Sexp))) (parse body))]
         [else (error 'IKEU "parse: duplicates parameters, given ~s" dup-value?)]))]
    ;; {begin Expr ...}
    [(list 'begin exprs ...) (begC (map parse exprs))]
    ;; {makearr Expr ...}
    [(list 'makearr args ...) (appC (idC 'makearr) (map parse args))]
    ;; {Expr Expr ...}
    [(list fun args ...) (appC (parse fun) (map parse args))]
    [other (error 'IKEU "parse: invalid expression, given ~e" sexp)]))

;; Header: Parse a type.
(define (parse-type [sexp : Sexp]) : Ty
  (match sexp
    ['num (numT)]
    ['bool (boolT)]
    ['str (strT)]
    ['numarray (numarrayT)]
    ['void (voidT)]
    [(list args ... '-> ret)
     (funT (map (lambda ([se : Sexp]) (parse-type se)) (cast args (Listof Sexp))) (parse-type ret))]
    [else (error 'IKEU "parse: ~e is not a typed" sexp)]))


;; Header: Type-check an expression.
(define (type-check [expr : TyExprC] [tenv : (Listof TyBinding)]) : Ty
  (match expr
    ;; numC
    [(numC n) (numT)]
    ;; idC
    [(idC n) (lookup-ty n tenv)]
    ;; strC
    [(strC s) (strT)]
    ;; binC
    [(binC id val) (type-check val tenv)]
    ;; conC
    [(conC if then else)
     (local ([define if-type (type-check if tenv)]
             [define then-type (type-check then tenv)]
             [define else-type (type-check else tenv)])
       (cond
         [(not (boolT? if-type))
          (error 'IKEU "type-check: if condition is not a bool type, given ~e" if-type)]
         [(not (equal? then-type else-type))
          (error 'IKEU "type-check: then and else are not the same type: then ~e, else ~e" then-type else-type)]
         [else then-type]))]
    ;; lamC
    [(lamC args argst body) (funT argst (type-check body (add-ty-env args argst tenv)))]
    ;; begC
    [(begC args)
     (local ([define tc-args (map (lambda ([arg : TyExprC]) (type-check arg tenv)) args)])
       (last tc-args))]
    ;; appC - makearr
    [(appC (idC 'makearr) args)
     (local ([define argst (map (lambda ([arg : TyExprC]) (type-check arg tenv)) args)])
       (cond
         [(equal? #t (foldl (lambda (v l) (and (numT? v) l)) #t argst)) (numarrayT)]
         [else (error 'IKEU "type-check: makearr's arguments is not numbers")]))]
    ;; appC
    [(appC fun args)
     (local ([define funt (type-check fun tenv)]
             [define argst (map (lambda ([arg : TyExprC]) (type-check arg tenv)) args)])
       (match funt
         [(funT funt-argst funt-rett)
          (cond
            [(equal? argst funt-argst) funt-rett]
            [else (error 'IKEU "type-check: type mismatch, params: ~e, args: ~e" argst (funT-argst funt))])]
         [other (error 'IKEU "type-check: ~e is not a function type" funt)]))]
    ;; recC
    [(recC name args argts rett body use)
     (local ([define extended-env (add-ty-env (list name) (list (funT argts rett)) tenv)])
       (cond
         [(equal? rett (type-check body (add-ty-env args argts extended-env)))
          (type-check use extended-env)]
         [else (error 'IKEU "type-check: return type is not correct, ~e" expr)]))]))

;; Header: Find the type given a Symbol and a Listof TyBinding.
(define (lookup-ty [for : Symbol] [env : (Listof TyBinding)]) : Ty
  (cond
    [(empty? env) (error 'IKEU "tc: ~s unbound identifier" for)]
    [(equal? for (TyBinding-name (first env))) (TyBinding-ty (first env))]
    [else (lookup-ty for (rest env))]))

;; Header: Extend new ty to base-tenv.
(define (add-ty-env [args : (Listof Symbol)] [argst : (Listof Ty)] [tenv : (Listof TyBinding)]) : (Listof TyBinding)
  (cond
    [(and (empty? args) (empty? argst)) tenv]
    [else (cons (TyBinding (first args) (first argst)) (add-ty-env (rest args) (rest argst) tenv))]))

;; Header: Interprets an expression, with a given environment.
(define (interp [expr : TyExprC] [env : (Listof Binding)]) : Value
  (match expr
    ;; numC
    [(numC n) n]
    ;; idC
    [(idC id) (unbox (lookup id env))]
    ;; strC
    [(strC str) str]
    ;; binC
    [(binC id expr) (set-box! (lookup id env) (interp expr env))]
    ;; lamC
    [(lamC args argst body) (closV args body env)]
    ;; conC
    [(conC if then else)
     (cond
       [(equal? (interp if env) #t) (interp then env)]
       [else (interp else env)])]
    ;; begC
    [(begC args)
     (local ([define interped-args (map (lambda ([arg : TyExprC]) (interp arg env)) args)])
       (last interped-args))]
    ;; recC
    [(recC name args argts rett body use)
     (local ([define new-env (add-env (list name) (list "dummy") env)]
             [define body-val (interp (lamC args argts body) new-env)])
       (set-box! (lookup name new-env) body-val)
       (interp use new-env))]
    ;; appC
    [(appC fun (list args ...))
     (local ([define fun-value (interp fun env)]
             [define args-value (map (lambda ([arg : TyExprC]) (interp arg env)) args)])
       (cond
         [(member fun-value '(+ - * / <= num-eq? str-eq? substring arr aref aset alen true false error makearr))
          (eval-prim-op fun-value args-value)]
         [(closV? fun-value) (interp (closV-body fun-value)
                  (add-env
                   (closV-args fun-value)
                   args-value
                   (closV-env fun-value)))]))]))

;; Header: Finds box given a symbol and a Listof Binding.
(define (lookup [for : Symbol] [env : (Listof Binding)]) : (Boxof Value)
  (cond
    [(equal? for (Binding-name (first env))) (Binding-box (first env))]
    [else (lookup for (rest env))]))

;; Header: Extends new env to base-env.
(define (add-env [s : (Listof Symbol)] [vals : (Listof Value)] [env : (Listof Binding)]) : (Listof Binding)
  (match s
    ['() env]
    [(cons f r) (cons (Binding f (box (first vals))) (add-env r (rest vals) env))]))

;; Header: Evaluate primitive operations.
(define (eval-prim-op [primative-op : Symbol] [args : (Listof Value)]) : Value
  (match (cons primative-op args)
    [(list '+ (? real? l) (? real? r)) (+ l r)]
    [(list '- (? real? l) (? real? r)) (- l r)]
    [(list '* (? real? l) (? real? r)) (* l r)]
    [(list '/ (? real? l) (? real? r))
     (cond
       [(equal? 0 r) (error 'IKEU "interp: division by zero")]
       [else (/ l r)])]
    [(list '<= (? real? l) (? real? r)) (<= l r)]
    [(list 'num-eq? (? real? l) (? real? r)) (equal? l r)]
    [(list 'str-eq? (? string? l) (? string? r)) (equal? l r)]
    [(list 'substring (? string? s) (? real? b) (? real? e)) (substring s (cast b Integer) (cast e Integer))]
    [(list 'arr s d) (make-vector (cast s Integer) (cast d Integer))]
    [(list 'aref (? vector? numarr) i) (vector-ref numarr (cast i Integer))]
    [(list 'aset (? vector? numarr) i n) (vector-set! numarr (cast i Integer) (cast n Real))]
    [(list 'alen (? vector? numarr)) (vector-length numarr)]
    [(list 'error (? string? s)) (error 'IKEU: "user-error: ~s" s)]
    [(list 'true) #t]
    [(list 'false) #f]
    [(list 'makearr a ...) (list->vector (cast args (Listof Real)))]))

;; Header: Convert a Value to a String
(define (serialize [val : Value]) : String
  (match val
    [(? real? n) (~v n)]
    [#t "true"]
    [#f "false"]
    [(? string? s) (~v s)]
    [(closV arg body env) "#<procedure>"]
    [(? void? val) "#<void>"]
    [(? vector? val) "#<array>"]))

;; TESTS
;; Tests with no error
(check-equal?
  (top-interp
   '{rec [num square-helper {[num n]} :
              {if {<= n 0} then 0 else {+ n {square-helper {- n 2}}}}]
      {let [{{num -> num} square  =
                          {{[num n]} : {square-helper {- {* 2 n} 1}}}}]
        {square 13}}}) "169")

(check-equal? 
  (top-interp
   '{let {[str a = "hello"]
          [str b = "hello"]}
      {str-eq? a b}}) "true")

(check-equal?
  (top-interp
   '{let {[num a = 10]
          [num b = 100]}
      {num-eq? a b}}) "false")

(check-equal? 
  (top-interp
   '{let
        {[str x = "Good Morning!"]}
      {substring x 0 4}}) "\"Good\"")

(check-equal? 
  (top-interp
   '{let
        {[numarray array = {arr 10 9}]}
      {aref array 0}}) "9")

(check-equal? 
  (top-interp
   '{let
        {[numarray array = {arr 10 9}]}
      {aset array 0 1}}) "#<void>")

(check-equal?
  (top-interp
   '{let {[num a = {+ 1 1}]
          [num b = {- 1 1}]
          [num c = {/ 1 1}]
          [num d = {* 1 1}]
          [bool e = {<= 1 1}]
          [numarray g = {arr 10 10}]
          [{numarray num num -> void} f = {{[numarray numarr] [num x] [num y]} : {aset numarr x y}}]
          [{num num -> bool} lesseq = {{[num x] [num y]} : {<= x y}}]}
      {f g a a}}) "#<void>")

(check-equal? 
  (top-interp
   '{let
        {[numarray array = {arr 10 20}]}
      {alen array}}) "10")

(check-equal? 
  (top-interp
   '{{[num x] [num y]} : {+ x y}}) "#<procedure>")

(check-equal? 
  (top-interp
   '{makearr 10 10 10}) "#<array>")

(check-equal? (top-interp '{let {[num x = 10]
                   [num y = 20]}
               {begin
                 {x := 100}
                 {+ 1 x}}}) "101")

;; Tests with error
(check-exn
 (regexp
  (regexp-quote "IKEU: parse: identifier if is not allowed"))
 (lambda () (top-interp
             '{let {[{num num -> num} sum = {{[num x] [num if]} -> {+ x y}}]}
                {sum 5 5}})))
(check-exn
 (regexp
  (regexp-quote "IKEU: interp: division by zero"))
 (lambda () (top-interp '{/ 1 0})))

(check-exn
 (regexp
  (regexp-quote "IKEU: type-check: if condition is not a bool type, given (numT)"))
 (lambda () (top-interp '{if 1 then 1 else 1})))

(check-exn
 (regexp
  (regexp-quote "IKEU: type-check: then and else are not the same type: then (numT), else (strT)"))
 (lambda () (top-interp '{if (<= 1 2) then 1 else "two"})))

(check-exn
 (regexp
  (regexp-quote "IKEU: tc: notx unbound identifier"))
 (lambda () (top-interp
             '{let {[num x = 10]}
                {+ notx 1}})))
(check-exn
 (regexp
  (regexp-quote "IKEU: type-check: makearr's arguments is not numbers"))
 (lambda () (top-interp '{makearr 1 1 1 "one"})))

(check-exn
 (regexp
  (regexp-quote "IKEU: type-check: type mismatch, params: (list (numT) (strT)), args: (list (numT) (numT))"))
 (lambda () (top-interp
             '{let {[{num num -> num} sum = {{[num x] [num y]} : {+ x y}}]}
                {sum 5 "five"}})))
(check-exn
 (regexp
  (regexp-quote "IKEU: type-check: (numT) is not a function type"))
 (lambda () (top-interp '{{1 1}}))) 

(check-exn
 (regexp
  (regexp-quote "IKEU: parse: args is not a list of symbols, given '(() ())"))
 (lambda () (top-interp '{{[num {}] [num {}]} : {+ x x}})))

(check-exn
 (regexp
  (regexp-quote "IKEU: parse: duplicates parameters, given x"))
 (lambda () (top-interp '{{[num x] [num x]} : {+ x x}})))

(check-exn
 (regexp
  (regexp-quote "IKEU: parse: duplicates parameters, given (x x)"))
 (lambda () (top-interp
             '{let {[num x = 10]
                    [num x = 20]}
                {+ x x}})))

(check-exn
 (regexp
  (regexp-quote "IKEU: parse: args is not a list of symbols, given '(() ())"))
 (lambda () (top-interp
             '{let {[num {} = 10]
                    [num {} = 20]}
                {+ x x}})))

(check-exn
 (regexp
  (regexp-quote "IKEU: parse: invalid expression, given '()"))
 (lambda () (top-interp '{})))

(check-exn
 (regexp
  (regexp-quote "IKEU: type-check: return type is not correct,"))
 (lambda () (type-check (recC 'something '(x) (list (strT)) (strT) (idC '*) (idC '-)) base-tenv)))

(check-exn
 (regexp
  (regexp-quote "IKEU:: user-error: \"this is an error\""))
 (lambda () (eval-prim-op 'error (list "this is an error"))))

(check-exn
 (regexp
  (regexp-quote "IKEU: parse: 14 is not a typed"))
 (lambda () (parse '(((((num -> 14) (str -> num) -> (bool -> bool)) a)) : 8))))
  
;; Tests from professor.
(check-equal? (top-interp '(if ((() : true)) then 3 else 4)) "3")

(check-equal? (top-interp '{let {[{bool bool -> bool} orfun = {{[bool x] [bool y]} : {if x then true else y}}]}
              {orfun (<= 1 1) (<= 1 1)}}) "true")

(check-equal? (eval-prim-op 'true '()) #t)

(check-equal? (eval-prim-op 'false '()) #f)