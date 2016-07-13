(ns status-im.contacts.views.new-contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.string :as str]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                image
                                                linear-gradient
                                                touchable-highlight]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.utils :refer [log on-error http-post toast]]
            [status-im.components.styles :refer [color-purple
                                                 color-white
                                                 icon-search
                                                 icon-back
                                                 icon-qr
                                                 toolbar-background1
                                                 toolbar-title-container
                                                 toolbar-title-text
                                                 button-input-container
                                                 button-input
                                                 form-text-input]]
            [status-im.qr-scanner.views.scan-button :refer [scan-button]]
            [status-im.i18n :refer [label]]
            [cljs.spec :as s]
            [status-im.contacts.validations :as v]
            [status-im.contacts.styles :as st]))


(def toolbar-title
  [view toolbar-title-container
   [text {:style toolbar-title-text}
    (label :t/add-new-contact)]])

(defview contact-name-input [name]
  []
  [text-field
   {:error (if (str/blank? name) "" nil)
    :errorColor "#7099e6"
    :value name
    :label           (label :t/name)
    :onChangeText          #(dispatch [:set-in [:new-contact :name] %])}])

(defview contact-whisper-id-input [whisper-identity error]
  []
  (let [error (if (str/blank? whisper-identity) "" error)
        error (if (s/valid? ::v/whisper-identity whisper-identity)
                error
                (label :t/enter-valid-address))]
    [view button-input-container
   [text-field
    {:error error
     :errorColor "#7099e6"
     :value whisper-identity
     :wrapperStyle                 (merge button-input)
     :label           (label :t/address)
     :onChangeText          #(do 
                               (dispatch [:set-in [:new-contact :whisper-identity] %])
                               (dispatch [:set :new-contact-address-error nil]))}]
   [scan-button {:showLabel (zero? (count whisper-identity))
                 :handler #(dispatch [:scan-qr-code {:toolbar-title (label :t/new-contact)} :set-new-contact-from-qr])}]]))

(defn on-add-contact [whisper-identity new-contact]
  (if (v/is-address? whisper-identity)
    (http-post "get-contacts-by-address" {:addresses [whisper-identity]}
               (fn [{:keys [contacts]}]
                 (if (> (count contacts) 0)
                   (let [contact (first contacts)
                         new-contact (merge 
                                       new-contact 
                                       {:address whisper-identity
                                        :whisper-identity (:whisper-identity contact)})]
                     (dispatch [:add-new-contact new-contact]))
                   (dispatch [:set :new-contact-address-error (label :t/unknown-address)]))))
    (dispatch [:add-new-contact new-contact])))

(defn toolbar-action [whisper-identity new-contact error]
  (let [valid-contact? (and
                         (s/valid? ::v/contact new-contact)
                         (nil? error))]
    {:image   {:source {:uri (if valid-contact?
                               :icon_ok_blue
                               :icon_ok_disabled)}
               :style  icon-search}
     :handler #(when valid-contact?
                 (let [contact (merge 
                                 {:photo-path (identicon whisper-identity)} 
                                 new-contact)]
                   (on-add-contact whisper-identity contact)))}))

(defview new-contact []
  [{:keys [name whisper-identity phone-number] :as new-contact} [:get :new-contact]
   error [:get :new-contact-address-error]]
  [view st/contact-form-container
   [toolbar {:background-color :white
             :nav-action       {:image   {:source {:uri :icon_back}
                                          :style  icon-back}
                                :handler #(dispatch [:navigate-back])}
             :custom-content   toolbar-title
             :action           (toolbar-action whisper-identity new-contact error)}]
   [view st/form-container
    [contact-name-input name]
    [contact-whisper-id-input whisper-identity error]]
   [view st/address-explication-container
    [text {:style st/address-explication} (label :t/address-explication)]]])
