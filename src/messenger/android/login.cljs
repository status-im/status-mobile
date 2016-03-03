(ns messenger.android.login
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android text-input]]
   [natal-shell.async-storage :refer [get-item set-item]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.android.resources :as res]
            [messenger.android.contacts-list :refer [contacts-list]]))

(set! js/PhoneNumber (js/require "awesome-phonenumber"))
(def country-code "US")

(defn show-home-view [nav]
  (binding [state/*nav-render* false]
    (.replace nav (clj->js {:component contacts-list
                            :name "contacts-list"}))))

(defn save-phone-number [nav]
  (let [phone-number (:user-phone-number @state/app-state)]
    (set-item "user-phone-number" phone-number)
    (show-home-view nav)))

(defn update-phone-number [value]
  (let [formatted (str (.getNumber (js/PhoneNumber. value country-code "international")))]
    (swap! state/app-state assoc :user-phone-number
           formatted)))

(defn load-user-phone-number []
  (get-item "user-phone-number"
            (fn [error value]
              (if (not error)
                (swap! state/app-state assoc :user-phone-number
                       value)
                (alert (str "error" error))))))

(defui Login
  static om/IQuery
  (query [this]
         '[:user-phone-number])
  Object
  (componentDidMount [this]
                     (load-user-phone-number))
  (render [this]
          (let [{:keys [user-phone-number]} (om/props this)
                {:keys [nav]} (om/get-computed this)]
            (view
             {:style {:flex 1
                      :backgroundColor "white"}}
             (toolbar-android {:logo res/logo-icon
                               :title "Login"
                               :titleColor "#4A5258"
                               :style {:backgroundColor "white"
                                       :height 56
                                       :elevation 2}})
             (view {:style {;; :alignItems "center"
                            }}
                   (text-input {:underlineColorAndroid "#9CBFC0"
                                :placeholder "Enter your phone number"
                                :keyboardType "phone-pad"
                                :onChangeText (fn [value]
                                                (update-phone-number value))
                                :style {:flex 1
                                        :marginHorizontal 18
                                        :lineHeight 42
                                        :fontSize 14
                                        :fontFamily "Avenir-Roman"
                                        :color "#9CBFC0"}}
                               user-phone-number)
                   (touchable-highlight {:onPress #(save-phone-number nav)
                                         :style {:alignSelf "center"
                                                 :borderRadius 7
                                                 :backgroundColor "#E5F5F6"
                                                 :width 100}}
                                        (text {:style {:marginVertical 10
                                                       :textAlign "center"}}
                                              "Sign in")))))))

(def login (om/factory Login))
