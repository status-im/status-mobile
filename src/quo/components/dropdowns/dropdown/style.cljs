(ns quo.components.dropdowns.dropdown.style)

(def gap 4)

(def blur-view
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(def left-icon
  {:margin-right gap})

(defn right-icon
  [label-color]
  {:color        label-color
   :margin-right gap})

(defn- add-styles-for-emoji
  [style size emoji-padding]
  (-> style
      (dissoc :padding-vertical :padding-left :padding-right)
      (assoc :width   size
             :padding emoji-padding)))

(defn root-container
  [{:keys [size border-radius padding-vertical padding-right padding-left emoji-padding]}
   {:keys [background-color border-color]}
   {:keys [icon? emoji? state]}]
  (cond-> {:height           size
           :align-items      :center
           :justify-content  :center
           :flex-direction   :row
           :padding-vertical padding-vertical
           :padding-left     (:default padding-left)
           :padding-right    padding-right
           :overflow         :hidden
           :background-color background-color
           :border-radius    border-radius}

    icon?
    (assoc :padding-left (:icon padding-left))

    emoji?
    (add-styles-for-emoji size emoji-padding)

    border-color
    (assoc :border-color border-color
           :border-width 1)

    (= state :disabled)
    (assoc :opacity 0.3)))
