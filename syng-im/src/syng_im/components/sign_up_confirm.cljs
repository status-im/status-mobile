(ns syng-im.components.sign-up-confirm
  (:require-macros
   [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text image touchable-highlight list-view
                                              toolbar-android text-input]]
            [syng-im.components.resources :as res]
            [syng-im.components.spinner :refer [spinner]]
            [syng-im.components.nav :as nav]
            [syng-im.utils.utils :refer [log toast http-post]]))

(defn show-home-view [navigator]
  (dispatch [:set-loading false])
  (nav/nav-push navigator {:view-id :contact-list}))

(defn sync-contacts [navigator]
  (dispatch [:sync-contacts #(show-home-view navigator)]))

(defn on-send-code-response [navigator body]
  (log body)
  (toast (if (:confirmed body)
           "Confirmed"
           "Wrong code"))
  (if (:confirmed body)
    ;; TODO user action required
    (sync-contacts navigator)
    (dispatch [:set-loading false])))

(defn code-valid? [code]
  (= 4 (count code)))

(defn send-code [code navigator]
  (when (code-valid? code)
    (dispatch [:set-loading true])
    (dispatch [:sign-up-confirm code (partial on-send-code-response navigator)])))

(defn update-code [value]
  (let [formatted value]
    (dispatch [:set-confirmation-code formatted])))

(defn sign-up-confirm-view [{:keys [navigator]}]
  (let [loading (subscribe [:get-loading])
        confirmation-code (subscribe [:get-confirmation-code])]
    (fn []
      [view {:style {:flex 1}}
       [view {:style {:flex 1
                      :backgroundColor "white"}}
        [toolbar-android {:logo res/logo-icon
                          :title "Confirm"
                          :titleColor "#4A5258"
                          :style {:backgroundColor "white"
                                  :height 56
                                  :elevation 2}}]
        [view {}
         [text-input {:underlineColorAndroid "#9CBFC0"
                      :placeholder "Enter confirmation code"
                      :keyboardType "number-pad"
                      :maxLength 4
                      :onChangeText (fn [value]
                                      (update-code value))
                      :style {:flex 1
                              :marginHorizontal 18
                              :lineHeight 42
                              :fontSize 14
                              :fontFamily "Avenir-Roman"
                              :color "#9CBFC0"}}
          @confirmation-code]
         (if (code-valid? @confirmation-code)
           [touchable-highlight {:onPress #(send-code @confirmation-code navigator)
                                 :style {:alignSelf "center"
                                         :borderRadius 7
                                         :backgroundColor "#E5F5F6"

                                         :width 100}}
            [text {:style {:marginVertical 10
                           :textAlign "center"}}
             "Confirm"]]
           [view {:style {:alignSelf "center"
                          :borderRadius 7
                          :backgroundColor "#AAB2B2"
                          :width 100}}
            [text {:style {:marginVertical 10
                           :textAlign "center"}}
             "Confirm"]])]]
       (when @loading
         [spinner {:visible true}])])))
