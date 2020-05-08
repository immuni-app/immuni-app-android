import { MarkdownString } from "danger/distribution/dsl/Aliases";
import { DangerDSLType } from "danger/distribution/dsl/DangerDSL";

declare var danger: DangerDSLType; // eslint-disable-line

declare function fail(
  message: MarkdownString,
  file?: string,
  line?: number
): void;
declare function message(
  message: MarkdownString,
  file?: string,
  line?: number
): void;
declare function warn(
  message: MarkdownString,
  file?: string,
  line?: number
): void;

const internalDanger = danger;

const internalFail = (
  msg: MarkdownString,
  file?: string,
  line?: number
): void => {
  if (!msg) {
    return;
  }

  return fail(msg.replace("\n", "\n\n"), file, line);
};

const internalMessage = (
  msg: MarkdownString,
  file?: string,
  line?: number
): void => {
  if (!msg) {
    return;
  }

  return message(msg.replace("\n", "\n\n"), file, line);
};

const internalWarn = (
  msg: MarkdownString,
  file?: string,
  line?: number
): void => {
  if (!msg) {
    return;
  }

  return warn(msg.replace("\n", "\n\n"), file, line);
};

export {
  internalDanger as danger,
  internalFail as fail,
  internalMessage as message,
  internalWarn as warn
};