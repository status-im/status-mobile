(ns status-im.transactions.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                scroll-view
                                                touchable-highlight
                                                touchable-opacity]]
            [status-im.components.styles :refer [icon-ok
                                                 icon-close
                                                 toolbar-title-container]]
            [status-im.components.carousel.carousel :refer [carousel]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.transactions.views.transaction-page :refer [transaction-page]]
            [status-im.transactions.styles :as st]
            [status-im.i18n :refer [label label-pluralize]]
            [clojure.string :as s]))


(defview confirm []
  [transactions [:transactions]
   {:keys [password]} [:get :confirm-transactions]
   wrong-password? [:wrong-password?]]
  [view st/transactions-screen
   [toolbar
    {:style          st/transactions-toolbar
     :nav-action     {:image   {:source {:uri :icon_close_white}
                                :style  icon-close}
                      :handler #(dispatch [:deny-transactions])}
     :custom-content [view {:style toolbar-title-container}
                      [text {:style st/toolbar-title-text}
                       (label-pluralize (count transactions) :t/confirm-transactions)]]
     :action         {:image   {:source {:uri (if-not (s/blank? password)
                                                :icon_ok
                                                :icon_ok_disabled_inversed)}
                                :style  icon-ok}
                      :handler #(dispatch [:accept-transactions password])}}]
   [view st/carousel-container
    [carousel {:pageStyle st/carousel-page-style
               :gap       16
               :count     (count transactions)
               :sneak     20}
     (when transactions
       (for [transaction transactions]
         [transaction-page transaction]))]]
   [view st/form-container
    [text-field
     {:inputStyle      st/password-style
      :secureTextEntry true
      :error           (when wrong-password? (label :t/wrong-password))
      :errorColor      :#ffffff80 #_:#7099e6
      :lineColor       :white
      :labelColor      :#ffffff80
      :value           password
      :label           (label :t/password)
      :onChangeText    #(dispatch [:set-in [:confirm-transactions :password] %])}]]])


;(re-frame.core/dispatch [:set :view-id :confirm])
