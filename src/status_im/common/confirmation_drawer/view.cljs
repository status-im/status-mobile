(ns status-im.common.confirmation-drawer.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.confirmation-drawer.style :as style]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn avatar
  [group-chat color display-name photo-path]
  (if group-chat
    [quo/group-avatar
     {:customization-color color
      :size                :size-20}]
    [quo/user-avatar
     {:full-name         display-name
      :profile-picture   photo-path
      :size              :xxs
      :ring?             false
      :status-indicator? false}]))

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
            theme                          (quo.theme/use-theme)
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
         [rn/view {:style (style/context-container theme)}
          [avatar group-chat color display-name photo-path]
          [quo/text
           {:weight :medium
            :size   :paragraph-2
            :style  {:margin-left 4}} display-name]]
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
