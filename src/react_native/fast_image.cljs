(ns react-native.fast-image
  (:require
    ["react-native-fast-image" :as FastImage]
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- build-source
  [source]
  (if (string? source)
    {:uri      source
     :priority :high}
    source))

(defn- remove-port
  [^js/Object source]
  (cond
    (string? source)               (string/replace-first source #":\d+" "")
    (.hasOwnProperty source "uri") (some-> source
                                     (oops/oget "uri")
                                     (string/replace-first #":\d+" ""))
    :else source))

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

;; We cannot use hooks since `reactify-component` seems to ignore the functional compiler
(defn- internal-fast-image
  [_]
  (let [loaded?         (reagent/atom false)
        error?          (reagent/atom false)
        on-image-error  (fn [event on-error]
                          (when (fn? on-error) (on-error event))
                          (reset! error? true))
        on-image-loaded (fn [event on-load]
                          (when (fn? on-load) (on-load event))
                          (reset! loaded? true)
                          (reset! error? false))]
    (fn [{:keys [source fallback-content on-error on-load] :as props}]
      [:> FastImage
       (assoc props
              :source   (build-source source)
              :on-error #(on-image-error % on-error)
              :on-load  #(on-image-loaded % on-load))
       (when (or @error? (not @loaded?))
         [placeholder
          {:style            (js->clj (:style props))
           :fallback-content fallback-content
           :error?           @error?
           :loaded?          @loaded?}])])))

(defn- compare-props
  [old-props new-props]
  ;; NOTE: We copy the object because during component tests the original is frozen
  (let [old-source      (some-> old-props (oops/oget "source") remove-port)
        new-source      (some-> new-props (oops/oget "source") remove-port)
        old-other-props (js-delete (js/Object.assign #js {} old-props) "source")
        new-other-props (js-delete (js/Object.assign #js {} new-props) "source")]
    (and (= old-source new-source)
         (= old-other-props new-other-props))))

(def fast-image
  (-> internal-fast-image
      (reagent/reactify-component)
      (rn/memo compare-props)
      (reagent/adapt-react-class)))
