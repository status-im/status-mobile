(ns status-im.ui.screens.browser.permissions.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.animation :as anim]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.styles :as styles]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]
            [status-im.models.browser :as model]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]))

(views/defview permissions-panel [{:keys [dapp? name] :as browser} {:keys [requested-permission params]}]
  (views/letsubs [dapp [:get-dapp-by-name name]
                  bottom-anim-value (anim/create-value -354)
                  alpha-value       (anim/create-value 0)
                  hide-panel        #(anim/start
                                      (anim/parallel
                                       [(anim/spring bottom-anim-value {:toValue -354})
                                        (anim/timing alpha-value {:toValue  0
                                                                  :duration 500})]))]
    {:component-did-mount #(anim/start
                            (anim/parallel
                             [(anim/spring bottom-anim-value {:toValue -20})
                              (anim/timing alpha-value {:toValue  0.6
                                                        :duration 500})]))}
    (let [_ (when-not requested-permission (js/setTimeout hide-panel 10))
          {:keys [dapp-name]} params
          {:keys [title description icon]} (get model/permissions requested-permission)]
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
         [components.common/button {:on-press #(re-frame/dispatch [:next-dapp-permission params])
                                    :label    (i18n/label :t/deny)}]
         [react/view {:width 16}]
         [components.common/button {:on-press #(re-frame/dispatch [:next-dapp-permission params requested-permission
                                                                   (:permissions-data params)])
                                    :label    (i18n/label :t/allow)}]]
        ;; TODO (andrey) will be in next PR
        #_[react/view {:flex-direction :row :margin-top 19}
           [icons/icon :icons/settings {:color colors/blue}]
           [react/text {:style styles/permissions-panel-permissions-label}
            (i18n/label :t/manage-permissions)]]]])))

;; NOTE (andrey) we need this complex function, to show animation before component will be unmounted
(defn permissions-anim-panel [browser show-permission]
  (let [timeout (atom nil)
        render? (reagent/atom false)]
    (fn [browser show-permission]
      (if show-permission
        (do
          (when @timeout
            (js/clearTimeout @timeout)
            (reset! timeout nil))
          (when-not @render? (reset! render? true)))
        (reset! timeout (js/setTimeout #(reset! render? false) 600)))
      (when @render?
        [permissions-panel browser show-permission]))))
