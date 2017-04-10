(ns flowquiz.game
  (:require [flowquiz.program-generator :as gen]
            [flowquiz.lang :as l]
            [clojure.string :as str])
  (:import java.awt.event.ActionListener
           javax.swing.JFrame
           javax.swing.JPanel
           javax.swing.JTextArea
           javax.swing.JLabel
           javax.swing.JTextField
           java.awt.GridBagLayout
           java.awt.GridBagConstraints)
  (:gen-class))

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

(defn- create-textfield [game-state]
  (let [text-field (JTextField.)]
    (doto text-field
      (.addActionListener (proxy [ActionListener] []
                            (actionPerformed [event]
                              (try ((:input-handler @game-state) (.getText text-field))
                                   (catch Exception ex
                                     (.printStackTrace ex)))))))))

(defn- add-components-to-pane [pane program status-label game-state]
  (let [ta (JTextArea. (prog->string program))
        font (.getFont ta)
        font (.deriveFont font  0 32.0)
        insets (java.awt.Insets. 4 4 4 4)]
    (.setLayout pane (GridBagLayout.))
    (.setFont ta font)
    (.add pane ta (gbc {:gridx 0
                        :gridy 0
                        :gridwidth 2
                        :insets insets
                        :both true}))
    (.add pane (JLabel. (str "enter values of "
                             (str/join ", " (l/vars
                                             program)))) (gbc {:gridy 1
                                                               :insets insets}))
    (.add pane (create-textfield game-state) (gbc {:gridy 1 :gridx 1
                                                   :insets insets
                                                   :both true}))
    (.add pane status-label (gbc {:gridy 2 :gridx 0
                                  :insets insets
                                  :both true}))))

(defn set-program [game-state  program]
  (let [{:keys [:frame :status-label]} @game-state]
    (swap! game-state assoc :program program)
    (-> frame .getContentPane
        .removeAll)
    (add-components-to-pane (.getContentPane frame) program status-label game-state)
    (.validate frame)
    (.repaint frame)))

(defn update-status [gs message]
  (-> @gs
      :status-label
      (.setText message)))

(defn answer-string [prog]
  (let [vars (l/vars prog)
        end-state (l/exec prog 100)]
    (str/join " "
              (map #(get end-state %) vars))))

(declare new-level)

(defn make-input-handler [answer game-state]
  (fn [attempt]
    (if (= attempt answer)
      (do
        (swap! game-state update :times conj (- (System/currentTimeMillis)
                                                (:start-time @game-state)))

        (swap! game-state assoc :start-time (System/currentTimeMillis))
        (swap! game-state update :score inc)
        (update-status game-state (str "Score " (:score @game-state)
                                       "Mistakes " (:mistakes @game-state)
                                       "Last time:" (quot (-> @game-state :times peek) 1000) " seconds"))
        (new-level game-state))
      (do
        (swap! game-state update :mistakes inc)
        (update-status game-state (str "Incorrect. Score "
                                       (:score @game-state) "
                                        Mistakes " (:mistakes
                                                    @game-state)))))))

(defn new-level [game-state]
  (let [program (gen/nice-program)
        answer (answer-string program)]
    (set-program game-state program)
    (swap! game-state assoc
           :input-handler (make-input-handler answer
                                              game-state)
           :start-time (System/currentTimeMillis))) game-state)

(defn -main [& args]
  (let [game-state (atom {:status-label (JLabel.)
                          :score 0
                          :mistakes 0
                          :frame (create-frame)
                          :times []})]
    (new-level game-state)
    (def gs game-state)
    game-state))



;;  (-> @gs :program answer-string)

;; ;; (-> @gs :times)

;; (-> @gs :program)

;; (map 
;;  #(l/exec '[[a = 1]
;;  [b = 16 - 12 / 11 * 11]
;;  [a = b]
;;  [b = 7 - a]
;;  [c = 13]
;;  [a = a - b]
;;  [c = b]
;;  [b = c + b]
;;  [a = c]
;;             [if c < b then goto 2]]
;;          %) (range 100))
