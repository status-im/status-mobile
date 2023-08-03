(ns quo2.components.switchers.group-messaging-card.view
  (:require
    [react-native.core :as rn]
    [quo2.components.avatars.group-avatar :as group-avatar]
    [quo2.components.switchers.base-card.view :as base-card]
    [quo2.components.switchers.card-main-info.view :as card-main-info]
    [quo2.components.switchers.card-content.view :as card-content]
    [quo2.components.switchers.group-messaging-card.style :as style]
    [quo2.components.switchers.utils :as utils]
    [quo2.foundations.colors :as colors]))

(defn view
  [{:keys [avatar type status title customization-color on-close content]}]
  (let [custom-color (colors/custom-color customization-color 60)]
    [base-card/base-card
     {:customization-color customization-color
      :on-close            on-close}
     (when avatar
       [rn/view {:style style/avatar-container}
        [group-avatar/group-avatar
         {:color custom-color
          :size  :large}]])
     [rn/view {:style style/content-container}
      [card-main-info/view
       {:title    title
        :subtitle (utils/subtitle type content)}]
      [card-content/view type status content]]]))
