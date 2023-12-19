(ns status-im.common.contact-list.view
  (:require
    [quo.core :as quo]))

(defn contacts-section-header
  [{:keys [title]}]
  [quo/divider-label title])
