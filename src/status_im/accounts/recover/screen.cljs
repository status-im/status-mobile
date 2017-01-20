(ns status-im.accounts.recover.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                linear-gradient
                                                touchable-highlight]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-gradient
                                                         toolbar-title-container
                                                         toolbar-title-text]]
            [status-im.components.styles :refer [color-purple
                                                 color-white
                                                 button-input]]
            [status-im.components.react :refer [linear-gradient]]
            [status-im.i18n :refer [label]]
            [status-im.accounts.recover.styles :as st]
            [status-im.accounts.recover.validations :as v]
            [cljs.spec :as s]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn toolbar-title []
  [view toolbar-title-container
   [text {:style toolbar-title-text
          :font  :medium}
    (label :t/recover-from-passphrase)]])

(defview passphrase-input [passphrase]
  [error [:get-in [:recover :passphrase-error]]]
  (let [error (if (str/blank? passphrase) "" error)
        error (if (s/valid? ::v/passphrase passphrase)
                error
                (label :t/enter-valid-passphrase))]
    [view
     [text-field
      {:value          passphrase
       :error          error
       :error-color    "#7099e6"
       :label          (label :t/passphrase)
       :label-color    "#838c93de"
       :line-color     "#0000001f"
       :input-style    st/input-style
       :wrapper-style  (merge button-input st/address-input-wrapper)
       :on-change-text #(dispatch [:set-in [:recover :passphrase] %])}]]))

(defview password-input [password]
  [error [:get-in [:recover :password-error]]]
  (let [error (if (str/blank? password) "" error)
        error (if (s/valid? ::v/password password)
                error
                (label :t/enter-valid-password))]
    [view
     [text-field
      {:value             password
       :secure-text-entry true
       :error             error
       :error-color       "#7099e6"
       :label             (label :t/password)
       :label-color       "#838c93de"
       :line-color        "#0000001f"
       :input-style       st/input-style
       :on-change-text    #(dispatch [:set-in [:recover :password] %])}]]))

(defview recover []
  [{:keys [passphrase password passphrase-error password-error]} [:get :recover]]
  (let [valid-form? (and
                      (s/valid? ::v/passphrase passphrase)
                      (s/valid? ::v/password password))
        gradient-colors ["rgba(24, 52, 76, 0.165)"
                         "rgba(24, 52, 76, 0.085)"
                         "rgba(24, 52, 76, 0)"]
        _ (log/debug passphrase " - " password)]
  [view st/screen-container
   [status-bar {:type :transparent}]
   [toolbar {:background-color :transparent
             :nav-action       (act/back #(dispatch [:navigate-back]))
             :custom-content   [toolbar-title]}]
   [linear-gradient {:locations [0 0.6 1]
                     :colors    gradient-colors
                     :style     toolbar-gradient}]
   [view st/recover-explain-container
    [text {:style st/recover-explain-text
           :font  :medium}
     (label :t/recover-explain)]]
   [view st/form-container
    [view st/form-container-inner
     [passphrase-input (or passphrase "")]
     [password-input (or password "")]]]
   [view st/bottom-actions-container
    [view st/recover-button-container
     [touchable-highlight
      {:on-press #(when valid-form?
                   (dispatch [:recover-account passphrase password]))}
      [view (st/recover-button valid-form?)
       [text {:style st/recover-button-text}
        (label :t/recover)]]]]]]))
