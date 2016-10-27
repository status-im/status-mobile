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
            [status-im.components.drawer.styles :as st]
            [status-im.profile.validations :as v]
            [status-im.resources :as res]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.i18n :refer [label]]
            [status-im.components.react :refer [dismiss-keyboard!]]
            [clojure.string :as str]
            [cljs.spec :as s]))

(defonce drawer-atom (atom))

(defn open-drawer []
  (.openDrawer @drawer-atom))

(defn close-drawer []
  (.closeDrawer @drawer-atom))

(defn user-photo [{:keys [photo-path]}]
  [image {:source (if (str/blank? photo-path)
                    res/user-no-photo
                    {:uri photo-path})
          :style  st/user-photo}])

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
     placeholder     (generate-gfy)]
    (fn []
      (let [{:keys [name photo-path status]} @account
            {new-name :name new-status :status} @profile]
        [view st/drawer-menu
         [touchable-without-feedback {:on-press #(dismiss-keyboard!)}
          [view st/drawer-menu
           [touchable-opacity {:on-press #(dispatch [:navigate-to :my-profile])}
            [view st/user-photo-container
             [user-photo {:photo-path photo-path}]]]
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
                                  (dispatch [:account-update {:name new-name}]))}]]
           [view st/status-container
            [text-input {:style               st/status-input
                         :editable            true
                         :multiline           true
                         :blur-on-submit      true
                         :maxLength           140
                         :accessibility-label :input
                         :placeholder         (label :t/profile-no-status)
                         :on-change-text      #(dispatch [:set-in [:profile-edit :status] %])
                         :on-blur             (fn []
                                                (when (and new-status (not (str/blank? new-status)))
                                                  (dispatch [:check-status-change new-status])
                                                  (dispatch [:account-update {:status new-status}])))
                         :default-value       status}]]
           [view st/menu-items-container
            [menu-item {:name    (label :t/profile)
                        :handler #(dispatch [:navigate-to :my-profile])}]
            [menu-item {:name    (label :t/settings)
                        :handler (fn []
                                   ;; TODO not implemented
                                   )}]
            [menu-item {:name    (label :t/discovery)
                        :handler #(dispatch [:navigate-to :discovery])}]
            [menu-item {:name    (label :t/contacts)
                        :handler #(dispatch [:navigate-to :contact-list])}]
            [menu-item {:name    (label :t/invite-friends)
                        :handler (fn []
                                   ;; TODO not implemented
                                   )}]
            [menu-item {:name    (label :t/faq)
                        :handler (fn [])}]]
           (when (= @keyboard-height 0)
             [view st/switch-users-container
              [touchable-opacity {:onPress (fn []
                                             (close-drawer)
                                             (dispatch [:navigate-to :accounts])
                                             ;; TODO not implemented
                                             )}
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
