(ns status-im.ui.screens.browser.permissions.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.browser.permissions :as browser.permissions]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.button :as button])
  (:require-macros [status-im.utils.views :as views]))

(defn hide-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         styles/panel-height
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0
                               :duration        500
                               :useNativeDriver true})])))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         20
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0.6
                               :duration        500
                               :useNativeDriver true})])))

(defn permission-details [requested-permission]
  (get browser.permissions/supported-permissions requested-permission))

(views/defview permissions-panel [[dapp? dapp dapps-account] {:keys [dapp-name]}]
  (views/letsubs [bottom-anim-value  (anim/create-value styles/panel-height)
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
      (let [{:keys [title description type icon]} @current-permission]
        [react/view styles/permissions-panel-container
         [react/animated-view {:style (styles/permissions-panel-background alpha-value)}]
         [react/animated-view {:style (styles/permissions-panel bottom-anim-value)}
          [react/view styles/permissions-panel-icons-container
           (if dapp?
             [chat-icon.screen/dapp-icon-permission dapp 40]
             [react/view styles/permissions-panel-dapp-icon-container
              [icons/icon :main-icons/dapp {:color colors/gray}]])
           [react/view {:margin-left 8 :margin-right 4}
            [react/view styles/dot]]
           [react/view {:margin-right 4}
            [react/view styles/dot]]
           [react/view {:margin-right 8}
            [react/view styles/dot]]
           [react/view styles/permissions-panel-ok-icon-container
            [icons/icon :tiny-icons/tiny-check styles/permissions-panel-ok-ico]]
           [react/view {:margin-left 8 :margin-right 4}
            [react/view styles/dot]]
           [react/view {:margin-right 4}
            [react/view styles/dot]]
           [react/view {:margin-right 8}
            [react/view styles/dot]]
           (if (= :wallet type)
             [chat-icon.screen/custom-icon-view-list (:name dapps-account) (:color dapps-account)]
             [react/view styles/permissions-panel-wallet-icon-container
              (when icon
                [icons/icon icon {:color colors/white}])])]
          [react/text {:style styles/permissions-panel-title-label :number-of-lines 2}
           (str "\"" dapp-name "\" " title)]
          (when (= :wallet type)
            [react/view styles/permissions-account
             [icons/icon :main-icons/account {:color (:color dapps-account)}]
             [react/view {:flex-shrink 1}
              [react/text {:numberOfLines 1
                           :style         {:margin-horizontal 6 :color (:color dapps-account) :typography :main-medium
                                           :font-size         13}}
               (:name dapps-account)]]])
          [react/text {:style styles/permissions-panel-description-label :number-of-lines 2}
           description]
          [react/view {:flex-direction :row :margin-top 24}
           [button/button
            {:theme    :red
             :style    {:flex 1}
             :on-press #(re-frame/dispatch [:browser.permissions.ui/dapp-permission-denied])
             :label    (i18n/label :t/deny)}]
           [button/button
            {:theme    :green
             :style    {:flex 1}
             :on-press #(re-frame/dispatch [:browser.permissions.ui/dapp-permission-allowed])
             :label    (i18n/label :t/allow)}]]]]))))
