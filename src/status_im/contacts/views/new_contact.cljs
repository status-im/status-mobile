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
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.utils :refer [log http-post]]
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
            [status-im.contacts.styles :as st]
            [status-im.components.styles :as cst]))


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
                         contact {:name             ""
                                  :address          id
                                  :photo-path       (identicon whisper-identity)
                                  :whisper-identity whisper-identity}]
                     (dispatch [:add-new-contact contact]))
                   (dispatch [:set :new-contact-address-error (label :t/unknown-address)]))))
    (dispatch [:add-new-contact {:name             ""
                                 :photo-path       (identicon id)
                                 :whisper-identity id}])))

(defn toolbar-action [new-contact-identity error]
  (let [valid-contact? (and
                         (s/valid? ::v/whisper-identity new-contact-identity)
                         (nil? error))]
    {:image   {:source {:uri (if valid-contact?
                               :icon_ok_blue
                               :icon_ok_disabled)}
               :style  icon-search}
     :handler #(when valid-contact?
                (on-add-contact new-contact-identity))}))

(defview contact-whisper-id-input [whisper-identity error]
  []
  (let [error (if (str/blank? whisper-identity) "" error)
        error (if (s/valid? ::v/whisper-identity whisper-identity)
                error
                (label :t/enter-valid-address))]
    [view button-input-container
     [text-field
      {:error          error
       :error-color    "#7099e6"
       :value          whisper-identity
       :wrapper-style  (merge button-input)
       :label          (label :t/address)
       :on-change-text #(do
                         (dispatch [:set-in [:new-contact-identity] %])
                         (dispatch [:set :new-contact-address-error nil]))}]
     [scan-button {:showLabel (zero? (count whisper-identity))
                   :handler   #(dispatch [:scan-qr-code {:toolbar-title (label :t/new-contact)} :set-contact-identity-from-qr])}]]))

(defview new-contact []
  [new-contact-identity [:get :new-contact-identity]
   error [:get :new-contact-address-error]]
  [view st/contact-form-container
   [view
    [status-bar]
    [toolbar {:background-color :white
              :nav-action       {:image   {:source {:uri :icon_back}
                                           :style  icon-back}
                                 :handler #(dispatch [:navigate-back])}
              :custom-content   toolbar-title
              :action           (toolbar-action new-contact-identity error)}]]
   [view st/form-container
    [contact-whisper-id-input new-contact-identity error]]
   [view st/address-explication-container
    [text {:style st/address-explication
           :font  :default}
     (label :t/address-explication)]]])
