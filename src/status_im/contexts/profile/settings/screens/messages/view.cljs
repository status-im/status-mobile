(ns status-im.contexts.profile.settings.screens.messages.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- open-blocked-users
  []
  (rf/dispatch [:open-modal :screen/settings-blocked-users]))

(defn view
  []
  [quo/overlay {:type :shell :top-inset? true}
   [quo/page-nav
    {:background :blur
     :icon-name  :i/arrow-left
     :on-press   navigate-back}]
   [quo/page-top {:title (i18n/label :t/messages)}]
   [quo/category
    {:label     (i18n/label :t/contacts)
     :data      [{:title    (i18n/label :t/blocked-users)
                  :on-press open-blocked-users
                  :blur?    true
                  :action   :arrow}]
     :blur?     true
     :list-type :settings}]])
