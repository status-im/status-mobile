(ns status-im2.common.home.style)

(def title-column
  {:flex-direction     :row
   :align-items        :center
   :height             56
   :padding-vertical   12
   :padding-horizontal 20
   :background-color   :transparent})

(def title-column-text
  {:accessibility-label :communities-screen-title
   :margin-right        6
   :weight              :semi-bold
   :size                :heading-1})

(defn unread-indicator
  [unread-count max-value]
  (let [right-offset (cond
                       (> unread-count max-value)
                       -14

                       ;; Greater than 9 means we'll need 2 digits to represent
                       ;; the text.
                       (> unread-count 9)
                       -10

                       :else -6)]
    {:position :absolute
     :top      -6
     :right    right-offset
     :z-index  4}))

(def left-section
  {:position :absolute
   :left     20
   :top      12})

(def right-section
  {:position       :absolute
   :right          20
   :top            12
   :flex-direction :row})

(def top-nav-container
  {:height 56})
