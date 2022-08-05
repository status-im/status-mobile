(ns quo2.screens.qr-screen
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.qr :as quo2]
            [re-frame.core :as re-frame]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]))

;This is the space where we show various selector options to use the QR code component
;I believe following are the options
;
;-> text input for link address
;-> drop down selector to see type of QR code
;with the following options :
;1) profile address
;2) wallet legacy
;3) wallet multichain

;(defn cool-preview []
;  (let [state  (reagent/atom {:link-to-qr static-qr-code-url})
;        link-to-qr  (reagent/cursor state [:link-to-qr])
;        qr-view-type (reagent/cursor state [:qr-view-type])]
;    (fn []
;      [rn/view {:margin-bottom 50
;                :padding       16}
;       [rn/view {:flex 1}
;        [preview/customizer state descriptor]]
;       [rn/view {:padding-vertical 60
;                 :flex-direction   :row
;                 :justify-content  :center}
;        [quo2/button (merge (dissoc @state
;                                    :theme :before :after)
;                            {:on-press #(println "Hello world!")}
;                            (when @above
;                                  {:above :main-icons2/placeholder})
;                            (when @before
;                                  {:before :main-icons2/placeholder})
;                            (when @after
;                                  {:after :main-icons2/placeholder}))
;         (if @icon :main-icons2/placeholder @label)]]])))


(defn preview-qr []
  [quo2/qr "profile"]
;  [rn/view {:background-color (:ui-background @colors/theme)
;            :flex             1}
;   [rn/flat-list {:flex                      1
;                  :keyboardShouldPersistTaps :always
;                  :header                    [cool-preview]
;                  :key-fn                    str}]]
)
