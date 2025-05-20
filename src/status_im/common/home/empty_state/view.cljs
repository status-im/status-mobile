(ns status-im.common.home.empty-state.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.home.empty-state.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [selected-tab tab->content]}]
  (let [{:keys [image title description action-text
                on-action-press]} (tab->content selected-tab)
        customization-color       (rf/sub [:profile/customization-color])]
    [rn/view {:style (style/empty-state-container)}
     [quo/empty-state
      {:customization-color customization-color
       :image               image
       :title               title
       :description         description}]
     (when action-text
       [quo/button
        {:type                :primary
         :on-press            on-action-press
         :customization-color customization-color
         :size                24}
        action-text])]))
