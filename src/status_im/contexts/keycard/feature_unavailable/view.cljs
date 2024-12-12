(ns status-im.contexts.keycard.feature-unavailable.view
  (:require
    [quo.core :as quo]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn on-upvote
  [feature-name]
  (rf/dispatch [:keycard/feature-unavailable-upvote {:feature-name feature-name}]))

(defn view
  [{:keys [feature-name]}]
  [:<>
   [quo/drawer-top
    {:title       (i18n/label :t/feature-unavailable)
     :description (i18n/label :t/feature-unavailable-for-keycard-description)}]
   [quo/information-box
    {:type  :default
     :icon  :i/info
     :style {:margin-top 8 :margin-horizontal 20}}
    [:<>
     (i18n/label :t/feature-unavailable-info)
     [quo/text
      {:style    {:text-decoration-line :underline}
       :size     :paragraph-2
       :on-press #(on-upvote feature-name)}
      (i18n/label :t/upvote-it)]]]])
