(ns status-im.components.drawer.view
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.string :as str]
            [cljs.spec :as s]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                drawer-layout
                                                touchable-without-feedback
                                                touchable-opacity]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.status-view.view :refer [status-view]]
            [status-im.components.drawer.styles :as st]
            [status-im.profile.validations :as v]
            [status-im.resources :as res]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.utils :refer [clean-text]]
            [status-im.i18n :refer [label]]
            [status-im.components.react :refer [dismiss-keyboard!]]
            [clojure.string :as str]
            [cljs.spec :as s]
            [status-im.components.chat-icon.screen :as ci]))

(defonce drawer-atom (atom))

(defn open-drawer []
  (.openDrawer @drawer-atom))

(defn close-drawer []
  (.closeDrawer @drawer-atom))

(defn menu-item [{:keys [name handler]}]
  [touchable-opacity {:style   st/menu-item-touchable
                      :onPress (fn []
                                 (close-drawer)
                                 (handler))}
   [text {:style st/menu-item-text
          :font  :default}
    name]])

(defn drawer-menu []
  (let
    [account         (subscribe [:get-current-account])
     profile         (subscribe [:get :profile-edit])
     keyboard-height (subscribe [:get :keyboard-height])
     placeholder     (generate-gfy)
     status-edit?    (r/atom false)]
    (fn []
      (let [{:keys [name photo-path status]} @account
            {new-name :name new-status :status} @profile]
        [view st/drawer-menu
         [touchable-without-feedback {:on-press #(dismiss-keyboard!)}
          [view st/drawer-menu
           [touchable-opacity {:on-press #(dispatch [:navigate-to :my-profile])}
            [view st/user-photo-container
             [ci/chat-icon photo-path {:size 64}]]]
           [view st/name-container
            [text-field
             {:line-color       :white
              :focus-line-color :white
              :placeholder      placeholder
              :editable         true
              :input-style      (st/name-input-text (s/valid? ::v/name (or new-name name)))
              :wrapper-style    st/name-input-wrapper
              :value            name
              :on-change-text   #(dispatch [:set-in [:profile-edit :name] %])
              :on-end-editing   #(when (and new-name (not (str/blank? new-name)))
                                  (dispatch [:account-update {:name (clean-text new-name)}]))}]]
           [view st/status-container
            (if @status-edit?
              [text-input {:style               st/status-input
                           :editable            true
                           :multiline           true
                           :auto-focus          true
                           :blur-on-submit      true
                           :focus               status-edit?
                           :maxLength           140
                           :accessibility-label :input
                           :placeholder         (label :t/profile-no-status)
                           :on-change-text      (fn [t]
                                                  (dispatch [:set-in [:profile-edit :status] (clean-text t)]))
                           :on-submit-editing   (fn []
                                                  (reset! status-edit? false)
                                                  (when (and new-status (not (str/blank? new-status)))
                                                    (dispatch [:check-status-change (clean-text new-status)])
                                                    (dispatch [:account-update {:status (clean-text new-status)}])))
                           :default-value       status}]
              [status-view {:style           st/status-text
                            :on-press        #(reset! status-edit? true)
                            :number-of-lines 3
                            :status          status}])]
           [view st/menu-items-container
            [menu-item {:name    (label :t/profile)
                        :handler #(dispatch [:navigate-to :my-profile])}]
            [menu-item {:name    (label :t/settings)
                        :handler (fn []
                                   ;; TODO not implemented
                                   )}]
            [menu-item {:name    (label :t/discover)
                        :handler #(dispatch [:navigate-to-tab :discover])}]
            [menu-item {:name    (label :t/contacts)
                        :handler #(dispatch [:navigate-to-tab :contact-list])}]]
           (when (zero? @keyboard-height)
             [text {:style st/feedback
                    :font  :default} (label :t/feedback)])
           (when (zero? @keyboard-height)
             [view st/switch-users-container
              [touchable-opacity {:onPress (fn []
                                             (close-drawer)
                                             (dispatch [:navigate-to :accounts]))}
               [text {:style st/switch-users-text
                      :font  :default}
                (label :t/switch-users)]]])]]]))))

(defn drawer-view [items]
  [drawer-layout {:drawerWidth          260
                  :renderNavigationView #(r/as-element [drawer-menu])
                  :onDrawerSlide        dismiss-keyboard!
                  :ref                  (fn [drawer]
                                          (reset! drawer-atom drawer))}
   items])
