(ns quo2.components.numbered-keyboard.numbered-keyboard.style)

(def container
  {:flex-direction     :row
   :flex-wrap          :wrap
   :flex               1
   :padding-horizontal 34
   :padding-top        8
   :padding-bottom     0})

(defn keyboard-item
  [position]
  {:width         "33%"
   :margin-bottom 12
   :align-items   (condp = (mod position 3)
                    1 :flex-start
                    2 :center
                    0 :flex-end)})
