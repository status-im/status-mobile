(ns status-im.contexts.wallet.sheets.account-origin.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.constants :as const]
    [status-im.contexts.wallet.sheets.account-origin.style :as style]
    [utils.i18n :as i18n]))

(defn- header
  [text]
  [quo/text
   {:weight :semi-bold
    :style  style/header-container}
   text])

(defn- description
  [text]
  [quo/text
   {:size  :paragraph-2
    :style style/desc-container}
   text])

(defn- section
  [title desc]
  [:<>
   [header title]
   [description desc]])

(defn view
  []
  [rn/view
   [quo/drawer-top {:title (i18n/label :t/account-origin-header)}]
   [description (i18n/label :t/account-origin-desc)]
   [section (i18n/label :t/origin-header) (i18n/label :t/origin-desc)]
   [section
    (i18n/label :t/derivation-path-header)
    (i18n/label :t/derivation-path-desc)]
   [quo/button
    {:type            :outline
     :size            24
     :icon-left       :i/info
     :container-style style/action-container
     :on-press        #(rn/open-url const/create-account-link)}
    (i18n/label :t/read-more)]])
