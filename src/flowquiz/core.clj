(ns flowquiz.core
  (:require [clojure.walk :as walk])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def program
  '[[a = 5]
    [b = 10]
    [c = \( 15 + 20 \)]
    [a = a + 5 * b]
    [if a > 50 end]
    [if a > 2 goto 2 else goto 3]])

(defn exec
  ([p]
   (exec p {}))
  ([p state]
   (map second p)))

(exec program)

(-> program
    first
    first
    name
    class)

(map class (flatten program))

(def operators '{= -50, < 45, goto 40, * 50, else 40, > 45, if 40, - 40, / 40, + 40, == 45})

(into {} (for [[k v] operators] [k (- 50 v)]))


operators

(into #{} (filter symbol? (flatten program)))

(defn kind
  [a]
  (cond (nil? a) :eof
        (operators a) :op
        (= \( a) :left-paren
        (= \) a) :right-paren
        (number? a) :number
        :else :var))

(defn ->rpn [tokens]
  (loop [tokens tokens
         stack []
         queue []]
    (let [[a & z] tokens
          type (kind a)]
      (cond
        (or (#{:number :var} type)) (recur z stack (conj queue a))
        (= :op type)                (let [t (peek stack)]
                                ;;all ops are left-associative
                                      (if (and t (operators t) (<= (operators a) (operators t)))
                                        (recur tokens (pop stack) (conj queue t))
                                        (recur z (conj stack a) queue)))
        (=  :left-paren type)       (recur z (conj stack a) queue)
        (=  :right-paren type)      (let [t (peek stack)]
                                      (if (= t \()
                                        (recur z (pop stack) queue)
                                        (recur tokens (pop stack) (conj queue t))))
        (= :eof type)         (concat queue (reverse stack))))))


