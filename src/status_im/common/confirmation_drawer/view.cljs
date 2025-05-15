(ns status-im.common.confirmation-drawer.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.confirmation-drawer.style :as style]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn extra-action-view
  [extra-action extra-text extra-action-selected?]
  (when extra-action
    [rn/view {:style {:margin-top 16 :flex-direction :row}}
     [quo/selectors
      {:type      :checkbox
       :on-change #(reset! extra-action-selected? %)}]
     [quo/text {:style {:margin-left 10}} extra-text]]))

(defn confirmation-drawer
  [{:keys [title description context button-text on-press extra-action extra-text accessibility-label
           close-button-text]}]
  (let [extra-action-selected? (reagent/atom false)]
    (fn []
      (let [{:keys [group-chat chat-id public-key color chat-type
                    profile-picture name]} context
            id                             (or chat-id public-key)
            [primary-name _]               (when-not (or group-chat
                                                         (= chat-type constants/public-chat-type))
                                             (rf/sub [:contacts/contact-two-names-by-identity id]))
            display-name                   (cond
                                             (= primary-name "Unknown")
                                             name
                                             (= primary-name nil)
                                             name
                                             :else
                                             primary-name)
            photo-path                     (or profile-picture (rf/sub [:chats/photo-path id]))]
        [rn/view
         {:style               {:margin-horizontal 20}
          :accessibility-label accessibility-label}
         [quo/text
          {:weight :semi-bold
           :size   :heading-2} title]
         [quo/context-tag
          {:type                (if group-chat :group :default)
           :profile-picture     photo-path
           :full-name           display-name
           :group-name          display-name
           :customization-color color
           :container-style     style/context-tag
           :size                24}]
         [quo/text description]
         [extra-action-view extra-action extra-text extra-action-selected?]
         [rn/view {:style style/buttons-container}
          [quo/button
           {:type            :grey
            :container-style {:flex 0.48} ;;WUT? 0.48 , whats that ?
            :on-press        #(rf/dispatch [:hide-bottom-sheet])}
           (or close-button-text (i18n/label :t/close))]
          [quo/button
           {:type            :danger
            :container-style {:flex 0.48}
            :on-press        #(do
                                (when @extra-action-selected? (extra-action))
                                (on-press))}
           button-text]]]))))
