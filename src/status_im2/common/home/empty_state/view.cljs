(ns status-im2.common.home.empty-state.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.common.home.empty-state.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [selected-tab tab->content]}]
  (let [{:keys [image title description]} (tab->content selected-tab)
        customization-color               (rf/sub [:profile/customization-color])]
    [rn/view {:style (style/empty-state-container)}
     [quo/empty-state
      {:customization-color customization-color
       :image               image
       :title               title
       :description         description}]]))
