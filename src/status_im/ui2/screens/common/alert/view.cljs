(ns status-im.ui2.screens.common.alert.view
  (:require
   [reagent.core :as reagent]
   [status-im.i18n.i18n :as i18n]
   [status-im.utils.re-frame :as rf]
   [quo2.components.markdown.text :as quo2.text]
   [react-native.core :as rn]
   [quo2.core :as quo2]
   [quo2.components.buttons.button :as quo2.button]
   [status-im.ui2.screens.common.alert.style :as style]
   [quo2.components.selectors.selectors :as selectors]))

(defn avatar [group-chat color display-name photo-path]
  (if group-chat
    [quo2/group-avatar {:color color
                        :size  :small}]
    [quo2/user-avatar {:full-name        display-name
                       :profile-picture  photo-path
                       :size             :xxs
                       :status-indicator false}]))

(defn extra-action-view [extra-action extra-text extra-action-selected?]
  (when extra-action
    [rn/view {:style {:margin-top 16 :flex-direction :row}}
     [selectors/checkbox {:on-change (fn [^js e] (reset! extra-action-selected? e))}]
     [quo2.text/text {:style {:margin-left 10}} extra-text]]))

(defn alert [{:keys [title description context button-text on-press extra-action extra-text]}]
  (let [extra-action-selected? (reagent/atom false)]
    (fn []
      (let [{:keys [group-chat chat-id public-key color name]} context
            id           (or chat-id public-key)
            display-name (if-not group-chat (first (rf/sub [:contacts/contact-two-names-by-identity id])) name)
            contact      (when-not group-chat (rf/sub [:contacts/contact-by-address id]))
            photo-path   (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path id]))]
        [rn/view {:style {:margin-horizontal 20}}
         [quo2.text/text {:weight :semi-bold
                          :size   :heading-1} title]
         [rn/view {:style (style/context-container)}
          [avatar group-chat color display-name photo-path]
          [quo2.text/text {:weight :medium
                           :size   :paragraph-2
                           :style  {:margin-left 4}} display-name]]
         [quo2.text/text description]
         [extra-action-view extra-action extra-text extra-action-selected?]
         [rn/view {:style (style/buttons-container)}
          [quo2.button/button {:type     :grey
                               :style    {:flex 0.48}
                               :on-press #(rf/dispatch [:bottom-sheet/hide])}
           (i18n/label :t/close)]
          [quo2.button/button {:type     :danger
                               :style    {:flex 0.48}
                               :on-press #(do
                                            (when @extra-action-selected? (extra-action))
                                            (on-press))}
           button-text]]]))))
