(ns status-im.contexts.wallet.send.routes.style)

(def routes-container
  {:padding-horizontal 20
   :flex-grow          1
   :padding-vertical   16
   :width              "100%"})

(def routes-header-container
  {:flex-direction  :row
   :justify-content :space-between})

(def routes-inner-container
  {:flex-direction  :row
   :justify-content :space-between})

(def section-label-right
  {:width 135})

(def section-label-left
  {:width 136})

(def network-links-container
  {:margin-horizontal -1.5
   :margin-top        7.5
   :z-index           3
   :flex              1})

(defn network-link-container
  [margin-top inverted?]
  (cond-> {:position :absolute
           :left     0
           :right    0
           :top      margin-top}
    inverted?
    (assoc :transform [{:scaleY -1}])))
