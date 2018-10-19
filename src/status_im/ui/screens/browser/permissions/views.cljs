(ns status-im.ui.screens.browser.permissions.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.browser.permissions :as browser.permissions]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(defn hide-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue -354})
     (anim/timing alpha-value {:toValue  0
                               :duration 500})])))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue -20})
     (anim/timing alpha-value {:toValue  0.6
                               :duration 500})])))

(defn permission-details [requested-permission]
  (get browser.permissions/supported-permissions requested-permission))

(views/defview permissions-panel [{:keys [dapp? dapp]} {:keys [dapp-name]}]
  (views/letsubs [bottom-anim-value  (anim/create-value -354)
                  alpha-value        (anim/create-value 0)
                  current-permission (reagent/atom nil)
                  update?            (reagent/atom nil)]
    {:component-will-update (fn [_ [_ _ {:keys [requested-permission]}]]
                              (cond
                                @update?
                                ;; the component has been updated with a new permission, we show the panel
                                (do (reset! update? false)
                                    (show-panel-anim bottom-anim-value alpha-value))

                                (and @current-permission requested-permission)
                                ;; a permission has been accepted/denied by the user, and there is
                                ;; another permission that needs to be processed by the user
                                ;; we hide the processed permission with an animation and update
                                ;; `current-permission` with a delay so that the information is still
                                ;; available during the animation
                                (do (reset! update? true)
                                    (js/setTimeout #(reset! current-permission
                                                            (permission-details requested-permission))
                                                   600)
                                    (hide-panel-anim bottom-anim-value alpha-value))

                                requested-permission
                                ;; the dapp is asking for a permission, we put it in current-permission
                                ;; and start the show-animation
                                (do (reset! current-permission
                                            (get browser.permissions/supported-permissions
                                                 requested-permission))
                                    (show-panel-anim bottom-anim-value alpha-value))

                                :else
                                ;; a permission has been accepted/denied by the user, and there is
                                ;; no other permission that needs to be processed by the user
                                ;; we hide the processed permission with an animation and update
                                ;; `current-permission` with a delay so that the information is still
                                ;; available during the animation
                                (do (js/setTimeout #(reset! current-permission nil) 500)
                                    (hide-panel-anim bottom-anim-value alpha-value))))}
    (when @current-permission
      (let [{:keys [title description icon]} @current-permission]
        [react/view styles/permissions-panel-container
         [react/animated-view {:style (styles/permissions-panel-background alpha-value)}]
         [react/animated-view {:style (styles/permissions-panel bottom-anim-value)}
          [react/view styles/permissions-panel-icons-container
           (if dapp?
             [chat-icon.screen/dapp-icon-permission dapp 48]
             [react/view styles/permissions-panel-dapp-icon-container
              [react/text {:style styles/permissions-panel-d-label} "Ð"]])
           [react/view {:margin-left 3 :margin-right 3}
            [react/view styles/dot]]
           [react/view {:margin-right 3}
            [react/view styles/dot]]
           [react/view styles/permissions-panel-ok-icon-container
            [icons/icon :icons/ok styles/permissions-panel-ok-ico]]
           [react/view {:margin-left 3 :margin-right 3}
            [react/view styles/dot]]
           [react/view {:margin-right 3}
            [react/view styles/dot]]
           [react/view styles/permissions-panel-wallet-icon-container
            (when icon
              [icons/icon icon {:color :white}])]]
          [react/text {:style styles/permissions-panel-title-label}
           (str "\"" dapp-name "\" " title)]
          [react/text {:style styles/permissions-panel-description-label}
           description]
          [react/view {:flex-direction :row :margin-top 14}
           [components.common/button
            {:on-press #(re-frame/dispatch [:browser.permissions.ui/dapp-permission-denied])
             :label    (i18n/label :t/deny)}]
           [react/view {:width 16}]
           [components.common/button
            {:on-press #(re-frame/dispatch [:browser.permissions.ui/dapp-permission-allowed])
             :label    (i18n/label :t/allow)}]]]]))))
