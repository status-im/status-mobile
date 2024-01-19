(ns react-native.fast-image
  (:require
    ["react-native-fast-image" :as FastImage]
    [react-native.pure :as rn.pure]))

(def fast-image-class (rn.pure/get-create-element-fn FastImage))

(defn fast-image-pure
  [{:keys [source] :as props}]
  (let [[loaded? set-loaded] (rn.pure/use-state false)
        [error? set-error]   (rn.pure/use-state false)]
    (fast-image-class
     (merge
      props
      {:source   (if (string? source)
                   {:uri      source
                    :priority :high}
                   source)
       :on-error (fn [e]
                   (when-let [on-error (:on-error props)]
                     (on-error e))
                   (set-error true))
       :on-load  (fn [e]
                   (when-let [on-load (:on-load props)]
                     (on-load e))
                   (set-loaded true)
                   (set-error false))})
     (when (or error? (not loaded?))
       (rn.pure/view {:style (merge (:style props)
                                    {:flex 1 :justify-content :center :align-items :center})}
                     (if error?
                       (rn.pure/text "X")
                       (when-not loaded?
                         (rn.pure/activity-indicator {:animating true}))))))))

(defn fast-image [props] (rn.pure/func fast-image-pure props))
