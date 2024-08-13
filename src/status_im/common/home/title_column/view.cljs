(ns status-im.common.home.title-column.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.home.title-column.style :as style]
    [status-im.common.plus-button.view :as plus-button]
    [utils.i18n :as i18n]))

(defn view
  [{:keys [beta? label handler accessibility-label customization-color]}]
  [rn/view style/title-column
   [rn/view
    {:style {:flex           1
             :align-items    :center
             :flex-direction :row}}
    [quo/text style/title-column-text
     label]
    (when beta?
      [rn/view
       {:style {:padding-top    6
                :padding-bottom 2}}
       [quo/tag
        {:accessibility-label :communities-chat-beta-tag
         :size                32
         :type                :label
         :label               (i18n/label :t/beta)
         :labelled?           true
         :blurred?            false}]])]

   (when handler
     [plus-button/plus-button
      {:on-press            handler
       :accessibility-label accessibility-label
       :customization-color customization-color}])])
