(ns flowquiz.program-generator
  (:require [flowquiz.lang :as l]))

(defn rand-comp-op []
  (rand-nth '[< <=  >  >= ==]))

(defn nth-var [n]
  (symbol (str (char (+ (int \a) n)))))

(defn- rand-var [max-vars]
  (nth-var  (rand-int max-vars)))

(repeatedly 10 #(rand-var 10))

(defn- rand-arit-op []
  (rand-nth '[+ - / *]))

(defn- rand-var-or-int [max-vars max-int]
  (if (zero? (rand-int (inc max-vars)))
    (rand-int max-int)
    (rand-var max-vars)))

(defn rand-expr
  ([max-vars max-int]
   (rand-expr max-vars max-int nil))
  ([max-vars max-int not-allowed]
   (let [help (fn []
                (if (zero? max-vars)
                  [(rand-int max-int)]
                  (loop [acc [(rand-var-or-int max-vars max-int)]]
                    (if (zero? (rand-int 2))
                      (recur (conj acc (rand-arit-op) (rand-var-or-int max-vars max-int)))
                      acc))))]
     (loop []
       (let [ret (help)]
         (if (= [not-allowed] ret)
           (recur)
           ret))))))

(defn rand-int-other-than[max excl]
  (let [r (rand-int (dec max))]
    (if (>= r excl) (inc r) r)))




(defn generate-program [max-vars max-lines max-int]
  (loop [program  [['a '= (rand-int max-int)]]
         var-count 1
         var-index (rand-int (Math/min (inc var-count) max-vars))]
    (if (< (count program) max-lines)
      (let [action (condp > (rand-int 10)
                     1 :if
                     ;;2 :goto
                     10 :assign)]
        (let []

          (recur (conj program (case action
                                 :if (let [start (-> ['if (rand-var var-count)]

                                                     (conj (rand-comp-op))
                                                     (into  (rand-expr var-count max-int))
                                                     (into ['then 'goto (rand-int max-lines)]))]
                                       (if (zero?  (rand-int 3))
                                         (into start ['else 'goto (rand-int max-lines)])
                                         start))
                                 :goto ['goto (rand-int max-lines)]
                                 :assign  (let [var (nth-var var-index)]
                                            (into [var '=] (rand-expr var-count max-int var)))))
                 (if (and (= action :assign) (>= var-index var-count))
                   (inc var-count)     var-count)
                 (rand-int-other-than (Math/min (inc var-count) max-vars) var-index))))

      program)))

(defn nice-program []
  (->> #(let [program (generate-program 3 10 20)]
          [program
           (try  (l/exec program 100)
                 (catch Exception ex ex))])
       repeatedly
       (filter (fn [[prog end-state]]
                 (and (map? end-state)
                      (not (:error end-state))
                      (>  100 (:instruction-count end-state) 20))))
       first
       first))


