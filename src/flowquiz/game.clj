(ns flowquiz.game
  (:require [flowquiz.program-generator :as gen]
            [flowquiz.lang :as l]
            [clojure.string :as str])
  (:import javax.swing.JFrame
           javax.swing.JPanel
           javax.swing.JTextArea
           javax.swing.JLabel
           javax.swing.JTextField
           java.awt.GridBagLayout
           java.awt.GridBagConstraints))

(defn create-frame []
  (doto (JFrame.)
    (.setSize 400 400)
    (.setVisible true)
    (.setDefaultCloseOperation 	javax.swing.WindowConstants/DISPOSE_ON_CLOSE)))

(defn prog->string [prog]
  (str/join "\n" (map (fn [[line-no line]] (str line-no \space (str/join " " line))) (map-indexed vector prog))))

(defn gbc [{:keys [:gridx :gridy :horizontal :vertical :weightx
                   :weighty :gridwidth :gridheight :none :both
                   :insets]}]
  (let [c (GridBagConstraints.)]
    (when horizontal (set! (. c fill)  GridBagConstraints/HORIZONTAL))
    (when vertical (set! (. c fill)  GridBagConstraints/VERTICAL))
    (when none (set! (. c fill)  GridBagConstraints/NONE))
    (when both (set! (. c fill)  GridBagConstraints/BOTH))
    (when gridx (set! (. c gridx) gridx))
    (when gridy (set! (. c gridy) gridy))
    (when weightx (set! (. c weightx) weightx))
    (when weighty (set! (. c weighty) weighty))
    (when gridheight (set! (. c gridheight) gridheight))
    (when gridwidth (set! (. c gridwidth) gridwidth))
    (when insets (set! (. c insets) insets))
    c))

(defn- add-components-to-pane [pane program]
  (let [ta (JTextArea. (prog->string program))
        font (.getFont ta)
        font (.deriveFont font  0 32.0)
        insets (java.awt.Insets. 4 4 4 4)]
    (.setLayout pane (GridBagLayout.))
    (.setFont ta font)
    (.add pane ta (gbc {:gridx 0
                        :gridy 0
                        :gridwidth 2
                        :insets insets}))
    (.add pane (JLabel. (str "enter values of " (str/join ", " (l/vars prog)))) (gbc {:gridy 1
                                                                                      :insets insets}))
    (.add pane (JTextField.) (gbc {:gridy 1 :gridx 1 :both true
                                   :insets insets}))))

(defn set-program [frame program]
  (-> frame .getContentPane
      .removeAll)
  (add-components-to-pane (.getContentPane frame) program)
  (.validate frame))

(set-program (create-frame) (gen/nice-program))

