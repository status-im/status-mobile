(ns quo2.components.common.no-flicker-image
  (:require
    [oops.core :as oops]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [reagent.core :as reagent]))


(def cached-sources (js/Set. (js/Array.)))

(defn- caching-image
  [_ on-source-loaded]
  (let [this (reagent/current-component)]
    (reagent/create-class
     {:component-did-update
      (fn []
        (let [source (-> this reagent/props :source)]
          (when (oops/ocall cached-sources "has" source)
            (on-source-loaded source))))
      :reagent-render
      (fn [{:keys [source] :as props}]
        [rn/image
         (assoc props
                ;; hide the cache image under the real one
                ;; have to render it for the on-load event
                :style {:width    1
                        :height   1
                        :left     "50%"
                        :top      "50%"
                        :position :absolute}
                :on-load
                (fn [_]
                  (when-not (oops/ocall cached-sources "has" source)
                    (oops/ocall cached-sources "add" source)
                    (on-source-loaded source)))
                :on-error js/console.error)])})))

(defn image
  "Same as rn/image but cache the image source in a js/Set, so the image won't
  flicker when re-render on android"
  []
  (let [loaded-source    (reagent/atom nil)
        on-source-loaded #(reset! loaded-source %)]
    (fn [props]
      (if platform/ios?
        [rn/image props]
        [:<>
         [rn/image (assoc props :source @loaded-source)]
         [caching-image props on-source-loaded]]))))
