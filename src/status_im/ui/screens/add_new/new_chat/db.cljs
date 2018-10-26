(ns status-im.ui.screens.add-new.new-chat.db
  (:require [status-im.utils.hex :as hex]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [cljs.spec.alpha :as spec]
            [clojure.string :as string]))

(defn own-public-key?
  [{:account/keys [account]} public-key]
  (= (:public-key account) public-key))

(defn validate-pub-key [db public-key]
  (cond
    (not (spec/valid? :global/public-key public-key))
    (i18n/label (if platform/desktop?
                  :t/use-valid-contact-code-desktop
                  :t/use-valid-contact-code))
    (own-public-key? db public-key)
    (i18n/label :t/can-not-add-yourself)))
