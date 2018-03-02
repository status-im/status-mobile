(ns status-im.ui.screens.profile.user.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.profile.user.styles :as styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.protocol.core :as protocol]))

(defn my-profile-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:my-profile/start-editing-profile])}
    [react/view
     [react/text {:style      common.styles/label-action-text
                  :uppercase? components.styles/uppercase?} (i18n/label :t/edit)]]]])

(defn my-profile-edit-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [toolbar/default-done {:handler   #(re-frame/dispatch [:my-profile/save-profile])
                          :icon      :icons/ok
                          :icon-opts {:color colors/blue}}]])

(def profile-icon-options
  [{:label  (i18n/label :t/image-source-gallery)
    :action #(re-frame/dispatch [:my-profile/update-picture])}
   {:label  (i18n/label :t/image-source-make-photo)
    :action (fn []
              (re-frame/dispatch [:request-permissions
                                  [:camera :write-external-storage]
                                  #(re-frame/dispatch [:navigate-to :profile-photo-capture])
                                  #(utils/show-popup (i18n/label :t/error)
                                                     (i18n/label :t/camera-access-error))]))}])

(defn qr-viewer-toolbar [label value]
  [toolbar/toolbar {}
   [toolbar/default-done {:icon-opts {:color colors/black}}]
   [toolbar/content-title label]
   [toolbar/actions [{:icon      :icons/share
                      :icon-opts {:color :black}
                      :handler   #(list-selection/open-share {:message value})}]]])

(defview ^{:theme :modal} qr-viewer []
  (letsubs [{:keys [value contact]} [:get :qr-modal]]
    [react/view {:flex-grow      1
                 :flex-direction :column}
     [qr-viewer-toolbar (:name contact) value]
     [qr-code-viewer/qr-code-viewer {}
      value (i18n/label :t/qr-code-public-key-hint) (str value)]]))

(defn- show-qr [contact source value]
  #(re-frame/dispatch [:navigate-to :profile-qr-viewer {:contact contact
                                                        :source  source
                                                        :value   value}]))

(defn share-contact-code [current-account public-key]
  [react/touchable-highlight {:on-press (show-qr current-account :public-key public-key)}
   [react/view styles/share-contact-code
    [react/view styles/share-contact-code-text-container
     [react/text {:style      styles/share-contact-code-text
                  :uppercase? components.styles/uppercase?}
      (i18n/label :t/share-contact-code)]]
    [react/view styles/share-contact-icon-container
     [vector-icons/icon :icons/qr {:color colors/blue}]]]])



(defn my-profile-settings [{:keys [network networks]}]
  [react/view
   [profile.components/settings-title (i18n/label :t/settings)]
   [profile.components/settings-item :t/main-currency "USD" #() false]
   [profile.components/settings-item-separator]
   [profile.components/settings-item :t/notifications "" #() true]
   [profile.components/settings-item-separator]
   [profile.components/settings-item :t/network (get-in networks [network :name])
    #(re-frame/dispatch [:navigate-to :network-settings]) true]
   (when config/offline-inbox-enabled?
     [profile.components/settings-item-separator])
   (when config/offline-inbox-enabled?
     [profile.components/settings-item :t/offline-messaging-settings ""
      #(re-frame/dispatch [:navigate-to :offline-messaging-settings]) true])])

(defn navigate-to-accounts []
  ;; TODO(rasom): probably not the best place for this call
  (protocol/stop-whisper!)
  (re-frame/dispatch [:navigate-to :accounts]))

(defn handle-logout []
  (utils/show-confirmation (i18n/label :t/logout-title)
                           (i18n/label :t/logout-are-you-sure)
                           (i18n/label :t/logout) navigate-to-accounts))

(defn logout []
  [react/view {}
   [react/touchable-highlight
    {:on-press handle-logout}
    [react/view profile.components.styles/settings-item
     [react/text {:style styles/logout-text
                  :font  (if platform/android? :medium :default)}
      (i18n/label :t/logout)]]]])


(defview my-profile []
  (letsubs [{:keys [public-key] :as current-account} [:get-current-account]
            editing?        [:get :my-profile/editing?]
            changed-account [:get :my-profile/profile]]
    (let [shown-account (merge current-account changed-account)]
      [react/view profile.components.styles/profile
       (if editing?
         [my-profile-edit-toolbar]
         [my-profile-toolbar])
       [react/scroll-view
        [react/view profile.components.styles/profile-form
         [profile.components/profile-header shown-account editing? true profile-icon-options :my-profile/update-name]]
        [react/view action-button.styles/actions-list
         [share-contact-code current-account public-key]]
        [react/view profile.components.styles/profile-info-container
         [my-profile-settings current-account]]
        [logout]]])))
