(ns flowquiz.game
  (:import javax.swing.JFrame
           javax.swing.JPanel
           javax.swing.JTextArea))

(defn create-frame[]
  (doto (JFrame.)
    (.setSize 400 400)
    (.setVisible true)
    (.setDefaultCloseOperation 	javax.swing.WindowConstants/DISPOSE_ON_CLOSE )))


(defn set-program[frame program]
  (-> frame .getContentPane
         .removeAll)
  (let [ta (JTextArea. (str program))]
    (.add frame ta)))


(set-program (create-frame))
