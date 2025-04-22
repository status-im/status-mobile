(ns quo.components.common.list.view
  (:require
    [quo.components.common.list.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn view
  [{:keys [data render-fn blur? container-style]}]
  (let [theme      (quo.context/use-theme)
        list-items (remove nil? data)
        last-index (dec (count list-items))]
    [rn/view {:style (merge (style/container blur? theme) container-style)}
     (doall
      (map-indexed
       (fn [index item]
         ^{:key index}
         [:<>
          (render-fn item)
          (when-not (= last-index index)
            [rn/view {:style (style/separator blur? theme)}])])
       list-items))]))
