(ns status-im.profile.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [cljs.spec :as s]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                icon
                                                modal
                                                scroll-view
                                                touchable-highlight
                                                touchable-opacity
                                                touchable-without-feedback
                                                show-image-picker
                                                dismiss-keyboard!]]
            [status-im.components.icons.custom-icons :refer [oct-icon]]
            [status-im.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.selectable-field.view :refer [selectable-field]]
            [status-im.components.qr-code :refer [qr-code]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.profile.handlers :refer [message-user]]
            [status-im.profile.validations :as v]
            [status-im.profile.styles :as st]
            [status-im.utils.random :refer [id]]
            [status-im.components.image-button.view :refer [show-qr-button]]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]))

(defn toolbar [{:keys [account edit?]}]
  (let [profile-edit-data-valid? (s/valid? ::v/profile account)]
    [view
     [touchable-highlight {:style    st/back-btn-touchable
                           :on-press (fn []
                                       (dispatch [:set-in [:profile-edit :edit?] false])
                                       (dispatch [:navigate-back]))}
      [view st/back-btn-container
       [icon :back st/back-btn-icon]]]
     [touchable-highlight {:style    st/actions-btn-touchable
                           :on-press (fn []
                                       (if edit?
                                         (when profile-edit-data-valid?
                                           (dismiss-keyboard!)
                                           (dispatch [:check-status-change (:status account)])
                                           (dispatch [:account-update account])
                                           (dispatch [:set-in [:profile-edit :edit?] false]))
                                         (dispatch [:set :profile-edit (merge account {:edit? true})])))}
      [view st/actions-btn-container
       (if edit?
         [oct-icon {:name  :check
                    :style (st/ok-btn-icon profile-edit-data-valid?)}]
         [icon :dots st/edit-btn-icon])]]]))

(defn- get-text
  [word]
  (let [props (merge {:key (id)}
                     (if (str/starts-with? word "#")
                       {:style st/hashtag}
                       {}))]
    [text props (str word " ")]))

(defn- highlight-tags
  [status]
  (->>
    (str/split status #" ")
    (map get-text)))

(defn status-image-view [_]
  (let [component         (r/current-component)
        set-status-height #(let [height (-> (.-nativeEvent %)
                                            (.-contentSize)
                                            (.-height))]
                            (r/set-state component {:height height}))]
    (r/create-class
      {:reagent-render
       (fn [{{:keys [name status photo-path]} :account
             edit?                            :edit?}]
         [view st/status-block
          [view st/user-photo-container

           (if edit?
             [touchable-highlight {:on-press (fn []
                                               (let [list-selection-fn (get platform-specific :list-selection-fn)]
                                                 (dispatch [:open-image-source-selector list-selection-fn])))}
              [view
               [my-profile-icon {:account {:photo-path photo-path
                                           :name       name}
                                 :edit?   edit?}]]]
             [my-profile-icon {:account {:photo-path photo-path
                                         :name       name}
                               :edit?   edit?}])]
          [text-field
           {:line-color       :white
            :focus-line-color :white
            :editable         edit?
            :input-style      (st/username-input edit? (s/valid? ::v/name name))
            :wrapper-style    st/username-wrapper
            :value            name
            :on-change-text   #(dispatch [:set-in [:profile-edit :name] %])}]
          [text-input {:style                  (st/status-input (:height (r/state component)))
                       :on-change              #(set-status-height %)
                       :on-content-size-change #(set-status-height %)
                       :maxLength              140
                       :multiline              true
                       :editable               edit?
                       :placeholder            (label :t/profile-no-status)
                       :on-change-text         #(dispatch [:set-in [:profile-edit :status] %])
                       :default-value          status}]])})))

(defview qr-modal []
  [qr [:get-in [:profile-edit :qr-code]]]
  [modal {:transparent    true
          :visible        (not (nil? qr))
          :animationType  :fade
          :onRequestClose #(log/debug "Nothing happens")}
   [touchable-without-feedback {:on-press #(dispatch [:set-in [:profile-edit :qr-code] nil])}
    [view st/qr-code-container
     [view st/qr-code
      [qr-code {:value (str "ethereum:" qr)
                :size  220}]]]]])

(defview profile []
  [{whisper-identity :whisper-identity
    address          :address
    username         :name
    photo-path       :photo-path
    phone            :phone
    status           :status
    :as              contact} [:contact]]
  [scroll-view {:style st/profile}
   [status-bar]
   [view
    [touchable-highlight {:style    st/back-btn-touchable
                          :on-press (fn []
                                      (dispatch [:navigate-back]))}
     [view (get-in platform-specific [:component-styles :toolbar-nav-action])
      [icon :back st/back-btn-icon]]]
    ;; TODO not implemented
    #_[touchable-highlight {:style    st/actions-btn-touchable
                          :on-press (fn []
                                      (.log js/console "Dots pressed!"))}
     [view st/actions-btn-container
      [icon :dots st/edit-btn-icon]]]]

   [status-image-view {:account    contact
                       :photo-path photo-path
                       :edit?      false}]

   [scroll-view (merge st/profile-properties-container {:keyboardShouldPersistTaps true
                                                        :bounces                   false})

    [view st/status-block
     [view st/btns-container
      [touchable-highlight {:onPress #(message-user whisper-identity)}
       [view st/message-btn
        [text {:style st/message-btn-text} (label :t/message)]]]
      ;; TODO not implemented
      #_[touchable-highlight {:onPress #(.log js/console "Not yet implemented")}
         [view st/more-btn
          [icon :more_vertical_blue st/more-btn-image]]]]]

    [view st/profile-property-with-top-spacing
     [selectable-field {:label     (label :t/phone-number)
                        :editable? false
                        :value     (if (and phone (not (str/blank? phone)))
                                     (format-phone-number phone)
                                     (label :t/not-specified))}]
     [view st/underline-container]]

    (when address
      [view st/profile-property
       [view st/profile-property-row
        [view st/profile-property-field
         [selectable-field {:label     (label :t/address)
                            :editable? false
                            :value     address}]]
        [show-qr-button {:handler #(dispatch [:set-in [:profile-edit :qr-code] address])}]]
       [view st/underline-container]])

    [view st/profile-property
     [view st/profile-property-row
      [view st/profile-property-field
       [selectable-field {:label     (label :t/public-key)
                          :editable? false
                          :value     whisper-identity}]]
      [show-qr-button {:handler #(dispatch [:set-in [:profile-edit :qr-code] whisper-identity])}]]]

    [view st/underline-container]

    [qr-modal]]])

(defview my-profile []
  [edit? [:get-in [:profile-edit :edit?]]
   qr [:get-in [:profile-edit :qr-code]]
   current-account [:get-current-account]
   changed-account [:get :profile-edit]]
  (let [{:keys [phone
                address
                public-key] :as account} (if edit?
                                           changed-account
                                           current-account)]
    [scroll-view {:style   st/profile
                  :bounces false}
     [status-bar]
     [toolbar {:account account
               :edit?   edit?}]

     [status-image-view {:account account
                         :edit?   edit?}]

     [scroll-view (merge st/my-profile-properties-container {:bounces false})
      [view st/profile-property
       [selectable-field {:label     (label :t/phone-number)
                          :editable? edit?
                          :value     (if (and phone (not (str/blank? phone)))
                                       (format-phone-number phone)
                                       (label :t/not-specified))}]
       [view st/underline-container]]

      [view st/profile-property
       [view st/profile-property-row
        [view st/profile-property-field
         [selectable-field {:label     (label :t/address)
                            :editable? edit?
                            :value     address}]]
        [show-qr-button {:handler #(dispatch [:set-in [:profile-edit :qr-code] address])}]]
       [view st/underline-container]]

      [view st/profile-property
       [view st/profile-property-row
        [view st/profile-property-field
         [selectable-field {:label     (label :t/public-key)
                            :editable? edit?
                            :value     public-key}]]
        [show-qr-button {:handler #(dispatch [:set-in [:profile-edit :qr-code] public-key])}]]]

      [view st/underline-container]

      [qr-modal]]]))
