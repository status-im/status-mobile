(ns react-native.fast-image
  (:require
    ["react-native-fast-image" :as FastImage]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(def internal-fast-image (reagent/adapt-react-class ^js FastImage))

(defn- placeholder
  [{:keys [style fallback-content error? loaded?]}]
  [rn/view
   {:style (assoc style
                  :flex            1
                  :justify-content :center
                  :align-items     :center)}
   (cond
     (and error? fallback-content) fallback-content
     error?                        [rn/text "X"]
     (not loaded?)                 [rn/activity-indicator {:animating true}])])

(defn- get-source
  [source]
  (if (string? source)
    {:uri      source
     :priority :high}
    source))

;; NOTE: We need to use ratoms to avoid the flickering since their state is updated
;; altogether at the end of the frame (different from hooks), this allows us to display
;; both uses of `internal-fast-image` always in sync.
(defn fast-image
  [_]
  (let [loaded?         (reagent/atom false)
        error?          (reagent/atom false)
        previous-source (reagent/atom nil)
        on-image-error  (fn [event on-error]
                          (when (fn? on-error) (on-error event))
                          (reset! error? true))
        on-image-loaded (fn [event on-load source]
                          (when (fn? on-load) (on-load event))
                          (reset! loaded? true)
                          (reset! error? false)
                          (reset! previous-source source))]
    (fn [{:keys [source fallback-content on-error on-load] :as props}]
      [internal-fast-image
       (assoc props
              :source   (get-source source)
              :on-error #(on-image-error % on-error)
              :on-load  #(on-image-loaded % on-load source))
       (cond
         @previous-source
         [internal-fast-image (assoc props :source (get-source @previous-source))]

         (or @error? (not @loaded?))
         [placeholder
          {:style            (:style props)
           :fallback-content fallback-content
           :error?           @error?
           :loaded?          @loaded?}])])))
