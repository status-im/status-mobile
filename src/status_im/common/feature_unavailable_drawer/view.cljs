(ns status-im.common.feature-unavailable-drawer.view
  (:require
    [quo.core :as quo]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn on-upvote
  []
  (rf/dispatch [:open-url constants/mobile-upvote-link]))

(defn view
  [{:keys [title description info-message]
    :or   {title        (i18n/label :t/feature-unavailable)
           description  (i18n/label :t/feature-unavailable-description)
           info-message (i18n/label :t/feature-unavailable-info)}}]
  [:<>
   [quo/drawer-top
    {:title       title
     :description description}]
   [quo/information-box
    {:type  :default
     :icon  :i/info
     :style {:margin-top 8 :margin-horizontal 20}}
    [:<>
     info-message
     [quo/text
      {:style    {:text-decoration-line :underline}
       :size     :paragraph-2
       :on-press on-upvote}
      (i18n/label :t/upvote-it)]]]])
