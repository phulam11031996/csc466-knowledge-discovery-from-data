#lang typed/racket
(require typed/rackunit)

;; Status: completed everything
;; define struct
(define-type ExprC (U NumC IdC StringC LamC CondC AppC))
(struct NumC ([n : Real]) #:transparent)
(struct IdC ([x : Symbol]) #:transparent)
(struct StringC ([s : String]) #:transparent)
(struct LamC ([args : (Listof Symbol)] [body : ExprC]) #:transparent)
(struct CondC ([if : ExprC] [statement : ExprC] [else : ExprC]) #:transparent)
(struct AppC ([func : ExprC] [args : (Listof ExprC)]) #:transparent)

;; binding and env
(struct Binding ([name : Symbol] [val : Value]) #:transparent) 
(define-type Env (Listof Binding))

(struct CloV ([args : (Listof Symbol)] [body : ExprC] [env : Env]) #:transparent)
(define-type Value (U Real Boolean String '+ '- '/ '* '<= 'equal? 'error CloV))

;; predefine top-env
(define top-env (list (Binding '+ '+) (Binding '- '-) (Binding '/ '/) (Binding '* '*) (Binding '<= '<=)
                      (Binding 'equal? 'equal?) (Binding 'error 'error) (Binding 'true true) (Binding 'false false)))

;; checking for restricted word
(define (check-restricted [s : Any]) : Boolean
  (match s
    ['if #t]
    ['vars: #t]
    ['body: #t]
    ['proc #t] 
    ['go #t]
    [else #f]))

; checking for primitive
(define (check-primitive [s : Any]) : Boolean
  (match s
    ['+ #t]
    ['- #t]
    ['/ #t]
    ['* #t]
    ['<= #t]
    ['equal? #t]
    ['error #t]
    [else #f]))

;; checking for duplicated in the list
(define (check-dup [l : (Listof Any)]) : Boolean
  (match l
    ['() #f]
    [(cons f r) (cond [(member f r) #t]
                      [else (check-dup r)])]))

;; checking for invalid symbol in list
(define (invalid-list-args [l : (Listof Any)]) : Boolean
  (match l
    ['() #f]
    [(cons f r) (cond [(check-restricted f) #t]
                      [else (invalid-list-args r)])]))

;; convert vars: ... body: to proc ... go
(define (convert-to-proc [s : Sexp]) : Sexp
  (match s
    [(list 'vars: (list a '= b) ... 'body: body)
     (cast (append (list (append '(proc) (list (cast a Sexp)) '(go) (list body)))  b) Sexp)]))

;; takes in the body of a function definition and creates
;; an ExprC so it can be evaluated 
(define (parse [s : Sexp]) : ExprC
  (match s
    [(? real? a) (NumC a)]
    [(? symbol? a) (cond [(check-restricted a) (error 'JYSS "parsing error")]
                         [else (IdC a)])] 
    [(? string? a) (StringC a)]
    [(? list? l)
     (match l
       [(list 'if a b c) (CondC (parse a) (parse b) (parse c))]
       [(list 'vars: (list a '= b) ... 'body: body) (parse (convert-to-proc s))]
       [(list 'proc (list (? symbol? args) ...) 'go body)
        (cond
          [(check-dup args) (error 'JYSS "parsing error with arguments")]
          [(invalid-list-args args) (error  'JYSS "parsing error with arguments")]
          [else (LamC (cast args (Listof Symbol)) (parse body))])]
       [(list f r ...) (AppC (parse f) (map parse r))]
       [else (error 'JYSS "parsing error")])]))
        
;; look for value in environment
(define (valInEnv [s : Symbol] [env : Env]) : Value

  (match env
    [(cons f r)
        (match f
          [(Binding n v) (cond
                           [(equal? n s) v]
                           [else (valInEnv s r)])])]
    [else (error 'JYSS "valInEnv: user-error")]))

;; make new env return env
(define (make-new-env [sym : (Listof Symbol)] [val : (Listof Value)]) : Env
  (match sym
    ['() '()]
    [(cons f r) (cons (Binding f (first val)) (make-new-env r (rest val)))]))

;;serialize turns output into a sring 
(define (serialize [val : Value]) : String
  (match val
    [(? real? n) (~v n)]
    [#t "true"] 
    [#f "false"]
    [(? string? s) (~v s)]
    [(CloV a b c) "#<procedure>"]
    [other "#<primop>"]))

;; interp-multi-args is a loop to interpret multiple argumentsin CloV 
(define (interp-multi-args [l : (Listof ExprC)] [env : Env]) : (Listof Value)
  (match l
    ['() '()]
    [(cons f r) (cons (interp f env) (interp-multi-args r env))]))


;;interprets the given expression, using the list of funs to resolve applications
(define (interp [exp : ExprC] [env : Env]) : Value
  (match exp
    [(NumC m) m]
    [(IdC v) (valInEnv v env)]
    [(StringC s) s]
    [(LamC args exp) (CloV args exp env)]
    [(CondC if statement else) (cond [(equal? (interp if env) #t) (interp statement env)]
                                     [(equal? (interp if env) #f) (interp else env)]
                                     [else (error 'JYSS "not true false")])]
    
    [(AppC func args) (define function (interp func env))
                      (match function
                        ['+ (match args
                              [(list f s) (cond [(and (real? (interp f env)) (real? (interp s env)))
                                                 (+ (cast (interp f env) Real) (cast (interp s env) Real))]
                                                [else (error 'JYSS "not a number")])]
                              [other (error 'JYSS "incorrect args for +")])]
                        ['- (match args
                              [(list f s) (cond [(and (real? (interp f env)) (real? (interp s env)))
                                                 (- (cast (interp f env) Real) (cast (interp s env) Real))]
                                                [else (error 'JYSS "not a number")])]
                              [other (error 'JYSS "incorrect args for -")])]
                        ['* (match args
                              [(list f s) (cond [(and (real? (interp f env)) (real? (interp s env)))
                                                 (* (cast (interp f env) Real) (cast (interp s env) Real))]
                                                [else (error 'JYSS "not a number")])]
                              [other (error 'JYSS "incorrect args for *")])]
                        ['/ (match args
                              [(list f s) (cond [(equal? (interp s env) 0) (error 'JYSS "divide by 0")]
                                                [(and (real? (interp f env)) (real? (interp s env)))
                                                 (/ (cast (interp f env) Real) (cast (interp s env) Real))]
                                                [else (error 'JYSS "not a number")])]
                              [other (error 'JYSS "incorrect args for /")])]
                        ['<= (match args
                              [(list f s) (cond [(and (real? (interp f env)) (real? (interp s env)))
                                                 (<= (cast (interp f env) Real) (cast (interp s env) Real))]
                                                [else (error 'JYSS "not a number")])]
                              [other (error 'JYSS "incorrect args for <=")])]
                        ['equal? (match args
                                   [(list f s) (cond [(check-primitive (interp f env))
                                                      (error 'JYSS "primitive for equal?")]
                                                     [(check-primitive (interp s env))
                                                      (error 'JYSS "primitive for equal?")]
                                                     [else (equal? (interp f env) (interp s env))])]
                                   [other (error 'JYSS "incorrect args for equal?")])]
                      
                        [(CloV a b e) (cond [(equal? (length a) (length args))
                                             (interp b (append (make-new-env a (interp-multi-args args env)) e))]
                                            [else (error 'JYSS "not the same length for args")])] 
                        ['error (match args
                                  [(cons a '()) (error 'JYSS (string-append "user-error " (serialize (interp a env))))]
                                  [else (error 'JYSS "invalid number of messages")])]
                        [other (error 'JYSS "interp error")])]))

;; is the high level interpreter for our programming language
(define (top-interp [s : Sexp]) : String
  (serialize (interp (parse s) top-env)))
(interp (AppC
 (LamC '(seven) (AppC (IdC 'seven) '()))
 (list
  (AppC
   (LamC '(minus) (LamC '() (AppC (IdC 'minus) (list (AppC (IdC '+) (list (NumC 3) (NumC 10))) (AppC (IdC '*) (list (NumC 2) (NumC 3)))))))
   (list (LamC '(x y) (AppC (IdC '+) (list (IdC 'x) (AppC (IdC '*) (list (NumC -1) (IdC 'y)))))))))) top-env)


