(ns status-im.ui.components.invite.advertiser
  (:require [status-im.ui.components.invite.modal :as modal]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.acquisition.core :as acquisition]
            [status-im.acquisition.advertiser :as advertiser]))

(defn accept-popover []
  (let [{:keys [rewardable]} @(re-frame/subscribe [::acquisition/metadata])]
    [modal/popover {:on-accept    #(re-frame/dispatch [::advertiser/decision :accept])
                    :on-decline   #(re-frame/dispatch [::advertiser/decision :decline])
                    :has-reward   rewardable
                    :accept-label (i18n/label :t/advertiser-starter-pack-accept)
                    :title        (if rewardable
                                    (i18n/label :t/advertiser-starter-pack-title)
                                    (i18n/label :t/advertiser-title))
                    :description  (if rewardable
                                    (i18n/label :t/advertiser-starter-pack-description)
                                    (i18n/label :t/advertiser-description))}]))
