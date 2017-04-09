(ns flowquiz.lang)

(declare exec-line)

(defn- do-goto [state line]
  (assoc  state :pc (second line)))

(defn- do-end [state line]
  (assoc state :end true))

(def operators '{* 50,
                 / 50,
                 - 40,
                 + 40,
                 < 30,
                 <= 30,
                 > 30,
                 >= 30,
                 == 30})

(defn token-type
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
          type (token-type a)]
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

(defn- perform-op [op stack]
  (let [rhs (peek stack)
        stack (pop stack)
        lhs (peek stack)
        stack (pop stack)]
    (conj stack
          (({'= =,
             '< <,
             '<= <=
             '>= >=
             '* *,
             '> >,
             '- -,
             '/ /,
             '+ +,
             '== =} op) lhs rhs))))

(defn- eval-expression [state expr]
  (let [st
        (reduce (fn [stack token]
                  (let [type (token-type token)]
                    (cond
                      (= type :op) (perform-op token stack)
                      (= type  :number) (conj stack token)
                      (= type  :var) (conj stack (get state token 0))
                      :else (str "unexpected " type token)))) [] (->rpn expr))]
    (first st)))

(defn- do-if [state [_ & z]]
  (let [[condition [_ & then-clause]]  (split-with #(not= % 'then) z)
        [then-clause [_ & else-clause]]  (split-with #(not= % 'else) then-clause)]
    (if (eval-expression state condition)
      (exec-line state then-clause)
      (if (seq else-clause) (exec-line state else-clause)
          state))))

(defn- do-assign [state line]
  (let [var (first line)
        expr (drop 2 line)]
    (let [v (int (eval-expression state expr))
          state (assoc state var v)]

      (if (< -1000 v 1000)
        state
        (assoc state :error :overflow)))))

(defn- do-error [state line]
  (assoc state :error line))

(defn line-fn [line]
  (let [a  (first line)]
    (cond (= 'if a) do-if
          (= 'goto a) do-goto
          (= 'end a) do-end
          (= '= (second line)) do-assign
          :else do-error)))

(defn exec-line [state line]
  (let [pc (:pc state)
        pc (if pc (inc pc) 1)]
    ((line-fn line) (assoc state :pc pc) line)))

(defn exec [p cpu-limit]
  (loop [state {:pc 0}
         instruction-count 0]

    (let [pc (:pc state)]
      (if (or (>= pc (count p)) (:end state) (:error state) (>= instruction-count cpu-limit))
        (assoc  state :instruction-count instruction-count)
        (recur (exec-line state (get p pc)) (inc instruction-count))))))

(defn vars [prog]
  (into (sorted-set)
        (comp (filter (fn [line] (= '= (second line))))
              (map first))
        prog))

