#include "desktopshortcuts.h"
#include "bridge.h"

#include "eventdispatcher.h"
#include <QDebug>
#include <QCoreApplication>
#include <QKeySequence>
#include <QEvent>
#include <QKeyEvent>

Q_LOGGING_CATEGORY(DESKTOPSHORTCUTS, "DesktopShortcuts")

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<DesktopShortcuts *>(); }
} registerMetaType;
} // namespace

DesktopShortcuts::DesktopShortcuts(QObject *parent)
    : QObject(parent) {
  QCoreApplication::instance()->installEventFilter(this);
  connect(this, &DesktopShortcuts::shortcutInvoked, this, &DesktopShortcuts::onShortcutInvoked);
}

DesktopShortcuts::~DesktopShortcuts() {
}

void DesktopShortcuts::setBridge(Bridge *bridge) {
  this->bridge = bridge;
}

QString DesktopShortcuts::moduleName() { return "DesktopShortcutsManager"; }

QList<ModuleMethod *> DesktopShortcuts::methodsToExport() {
  return QList<ModuleMethod *>{};
}

QVariantMap DesktopShortcuts::constantsToExport() { return QVariantMap(); }

void DesktopShortcuts::registerShortcuts(const QStringList& shortcuts) {
  //qCDebug(DESKTOPSHORTCUTS) << "registerShortcuts" << shortcuts << " " << shortcuts.size();
  this->registeredShortcuts = shortcuts;
}

bool DesktopShortcuts::eventFilter(QObject* obj, QEvent* event) {
  if (event->type() == QEvent::KeyPress) {
    QKeyEvent* ke = static_cast<QKeyEvent*>(event);

    QString modifier;

    if (ke->modifiers() & Qt::ShiftModifier) {
      modifier += "Shift+";
    }
    if (ke->modifiers() & Qt::ControlModifier) {
      modifier += "Ctrl+";
    }
    if (ke->modifiers() & Qt::AltModifier) {
      modifier += "Alt+";
    }
    if (ke->modifiers() & Qt::MetaModifier) {
      modifier += "Meta+";
    }
    QString key = QKeySequence(ke->key()).toString();

    //qCDebug(DESKTOPSHORTCUTS) << "### arrow " << key;
    if (registeredShortcuts.contains(modifier+key)) {
      emit shortcutInvoked(modifier+key);
      return true;
    }
    else {
      return false;
    }
  }
  else {
    return QObject::eventFilter(obj, event);
  }
}

void DesktopShortcuts::onShortcutInvoked(const QString& shortcut) {
  //qCDebug(DESKTOPSHORTCUTS) << "onShortcutInvoked " << shortcut << " " << registeredShortcuts.size();
  bridge->eventDispatcher()->sendDeviceEvent("shortcutInvoked", QVariantList{shortcut});
}

