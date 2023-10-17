(ns quo2.components.navigation.top-nav.style)

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

(def unread-dot
  {:position :absolute
   :top      -2
   :bottom   0
   :right    0
   :left     38})

(def right-section
  {:flex-direction :row})

(def top-nav-container
  {:height 56})

(def top-nav-inner-container
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :align-items     :center})
