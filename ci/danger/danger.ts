// danger.ts
// Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
// Please refer to the AUTHORS file for more information.
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

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
