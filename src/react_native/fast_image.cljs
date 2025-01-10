(ns react-native.fast-image
  (:require
    ["react-native-fast-image" :as FastImage]
    [clojure.string :as string]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.transforms :as transforms]))

(defn- build-source
  [source]
  (if (string? source)
    {:uri      source
     :priority :high}
    source))

(defn- remove-port
  [source]
  (cond
    (string? source) (string/replace-first source #":\d+" "")
    (:uri source)    (some-> source
                             :uri
                             (string/replace-first #":\d+" ""))
    :else            source))

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
  (let [old-props-clj (transforms/js->clj old-props)
        new-props-clj (transforms/js->clj new-props)
        old-source    (some-> old-props-clj
                              :source
                              remove-port)
        new-source    (some-> new-props-clj
                              :source
                              remove-port)]
    (and (= old-source new-source)
         (= (dissoc old-props-clj :source) (dissoc new-props-clj :source)))))

(def fast-image
  (-> internal-fast-image
      (reagent/reactify-component)
      (rn/memo compare-props)
      (reagent/adapt-react-class)))
