(ns react-native.utils)

(defonce ^:private throttle (atom {}))

(defn- wrapped-ref
  [{:keys [ref]} throttle-id]
  (fn [ref-value]
    (when ref-value
      (when ref
        (ref ref-value))
      (reset! throttle-id ref-value))))

(defn- throttled-on-press
  [{:keys [on-press allow-multiple-presses? throttle-duration]} throttle-id]
  (if allow-multiple-presses?
    on-press
    (fn [event]
      (let [id @throttle-id]
        (when (and id (not (get @throttle id)))
          (swap! throttle assoc id true)
          (on-press event)
          (js/setTimeout
           #(swap! throttle dissoc id)
           (or throttle-duration 500)))))))

(defn custom-pressable-props
  [{:keys [on-press] :as props}]
  (let [throttle-id (atom nil)]
    (cond-> props
      on-press
      (assoc :on-press (throttled-on-press props throttle-id)
             :ref      (wrapped-ref props throttle-id)))))

(defn get-props-and-children
  [argv]
  (let [first-child (first argv)
        props       (when (map? first-child) first-child)
        children    (if props (rest argv) argv)]
    [props children]))
