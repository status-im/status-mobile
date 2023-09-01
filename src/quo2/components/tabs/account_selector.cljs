(ns quo2.components.tabs.account-selector
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.markdown.text :as quo2]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def themes
  {:light {:default     {:bg           colors/neutral-10
                         :account-text colors/neutral-100
                         :label-text   colors/neutral-50}
           :transparent {:bg           colors/neutral-80-opa-5
                         :account-text colors/neutral-100
                         :label-text   colors/neutral-80-opa-40}}

   :dark  {:default     {:bg           colors/neutral-80-opa-80
                         :account-text colors/white
                         :label-text   colors/neutral-40}
           :transparent {:bg           colors/white-opa-5
                         :account-text colors/white
                         :label-text   colors/neutral-40}}})

(defn account-container-row
  [background-color]
  {:padding-vertical 4
   :flex-direction   :row
   :align-items      :center
   :background-color background-color
   :border-radius    12})

(def account-avatar-container
  {:margin-left  4
   :margin-right 8})

(defn get-color-by-type
  [type k]
  (get-in themes [(theme/get-theme) type k]))

(defn account-selector
  "[account-selector opts]
   opts
   {:show-label?       true/false            ;; hide or show the label
    :transparent?      true/false            ;; implement transparent background styles
    :style             style                 ;; any other styling can be passed
    :label-text        \"Label\"             ;; content to show where the label should be shown
    :account-text      \"My Savings\"        ;; content in place of account name
   }"
  [{:keys [show-label? account-text account-emoji transparent? label-text style]}]
  (let [background-color   (get-color-by-type (if transparent? :transparent :default) :bg)
        account-text-color (get-color-by-type (if transparent? :transparent :default) :account-text)
        label-text-color   (get-color-by-type (if transparent? :transparent :default) :label-text)]
    [rn/view {:style style}
     (when show-label?
       [quo2/text
        {:weight :medium
         :size   :paragraph-2
         :style  {:color         label-text-color
                  :margin-bottom 8}}
        label-text])
     [rn/view {:style (account-container-row background-color)}
      [rn/view {:style account-avatar-container}
       [account-avatar/view {:emoji account-emoji :size 32}]]
      [quo2/text
       {:weight :medium
        :size   :paragraph-1
        :style  {:color account-text-color}}
       account-text]]]))
