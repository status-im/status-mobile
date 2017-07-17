(ns status-im.contacts.views.new-contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.string :as str]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-title-container
                                                         toolbar-title-text
                                                         toolbar-background1]]
            [status-im.utils.utils :refer [http-post]]
            [status-im.components.styles :refer [icon-ok
                                                 button-input-container
                                                 button-input
                                                 color-blue]]
            [status-im.components.image-button.view :refer [scan-button]]
            [status-im.i18n :refer [label]]
            [cljs.spec.alpha :as s]
            [status-im.contacts.validations :as v]
            [status-im.contacts.styles :as st]
            [status-im.data-store.contacts :as contacts]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.hex :refer [normalize-hex]]
            [status-im.utils.platform :refer [platform-specific]]))


(def toolbar-title
  [view toolbar-title-container
   [text {:style toolbar-title-text}
    (label :t/add-new-contact)]])

(defn on-add-contact [id]
  (if (v/is-address? id)
    (http-post "get-contacts-by-address" {:addresses [id]}
               (fn [{:keys [contacts]}]
                 (if (> (count contacts) 0)
                   (let [{:keys [whisper-identity]} (first contacts)
                         contact {:name             (generate-gfy)
                                  :address          id
                                  :photo-path       (identicon whisper-identity)
                                  :whisper-identity whisper-identity}]
                     (if (contacts/exists? whisper-identity)
                       (dispatch [:add-pending-contact whisper-identity])
                       (dispatch [:add-new-contact contact])))
                   (dispatch [:set :new-contact-public-key-error (label :t/unknown-address)]))))
    (if (contacts/exists? id)
      (dispatch [:add-pending-contact id])
      (dispatch [:add-new-contact {:name             (generate-gfy)
                                   :photo-path       (identicon id)
                                   :whisper-identity id}]))))

(defn- validation-error-message
  [whisper-identity {:keys [address public-key]} error]
  (cond
    (#{(normalize-hex address) (normalize-hex public-key)}
      (normalize-hex whisper-identity))
    (label :t/can-not-add-yourself)

    (not (s/valid? ::v/whisper-identity whisper-identity))
    (label :t/enter-valid-public-key)

    (not (s/valid? ::v/contact-can-be-added whisper-identity))
    (label :t/contact-already-added)

    :else error))

(defn toolbar-actions [new-contact-identity account error]
  (let [error-message (validation-error-message new-contact-identity account error)]
    [{:image   {:source {:uri (if (str/blank? error-message)
                                :icon_ok_blue
                                :icon_ok_disabled)}
                :style  icon-ok}
      :handler #(when (str/blank? error-message)
                 (on-add-contact new-contact-identity))}]))

(defview contact-whisper-id-input [whisper-identity error]
  [current-account [:get-current-account]]
  (let [error (when-not (str/blank? whisper-identity)
                (validation-error-message whisper-identity current-account error))]
    [view button-input-container
     [text-field
      {:error          error
       :error-color    color-blue
       :input-style    st/qr-input
       :value          whisper-identity
       :wrapper-style  button-input
       :label          (label :t/public-key)
       :on-change-text #(do
                          (dispatch [:set-in [:new-contact-identity] %])
                          (dispatch [:set :new-contact-public-key-error nil]))}]
     [scan-button {:show-label? (zero? (count whisper-identity))
                   :handler     #(dispatch [:scan-qr-code
                                            {:toolbar-title (label :t/new-contact)}
                                            :set-contact-identity-from-qr])}]]))


(defview new-contact []
  [new-contact-identity [:get :new-contact-identity]
   error [:get :new-contact-public-key-error]
   account [:get-current-account]]
  [view st/contact-form-container
   [status-bar]
   [toolbar {:background-color toolbar-background1
             :style            (get-in platform-specific [:component-styles :toolbar])
             :nav-action       (act/back #(dispatch [:navigate-back]))
             :title            (label :t/add-new-contact)
             :actions          (toolbar-actions new-contact-identity account error)}]
   [view st/form-container
    [contact-whisper-id-input new-contact-identity error]]
   [view st/address-explication-container
    [text {:style st/address-explication
           :font  :default}
     (label :t/address-explication)]]])
