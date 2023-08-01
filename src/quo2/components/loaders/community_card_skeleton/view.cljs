(ns quo2.components.loaders.community-card-skeleton.view
  (:require [quo2.theme :as theme]
            [react-native.core :as rn]
            [quo2.components.loaders.community-card-skeleton.style :as style]))


(defn- internal-view
  [{:keys [width theme]}]
  [rn/view {:style (style/card width theme)}
   [rn/view (style/cover-container theme)]
   [rn/view (style/content-container theme)
    [rn/view (style/avatar theme)]
    [rn/view (style/lock theme)]
    [rn/view
     {:style style/card-content-container}
     [rn/view {:style (style/content-line theme 84 0)}]
     [rn/view {:style (style/content-line theme 311 8)}]
     [rn/view {:style (style/content-line theme 271 8)}]]
    [rn/view
     {:style style/stats-container}
     [rn/view {:style (style/stat-circle theme 0)}]
     [rn/view {:style (style/stat-line theme 4)}]
     [rn/view {:style (style/stat-circle theme 12)}]
     [rn/view {:style (style/stat-line theme 4)}]]
    [rn/view
     {:style style/tags-container}
     [rn/view {:style (style/tag theme 0)}]
     [rn/view {:style (style/tag theme 8)}]
     [rn/view {:style (style/tag theme 8)}]]]])

(def view (theme/with-theme internal-view))
