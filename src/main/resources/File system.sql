CREATE SCHEMA "filesystem";

CREATE SCHEMA "accounts";

CREATE SCHEMA "jobs";

CREATE TABLE "filesystem"."folder" (
  "id" uuid PRIMARY KEY NOT NULL,
  "name" string NOT NULL,
  "path" string NOT NULL,
  "parentFolder" uuid,
  "createdAt" datetime NOT NULL,
  "updatedAt" datetime NOT NULL
);

CREATE TABLE "filesystem"."file" (
  "id" uuid PRIMARY KEY NOT NULL,
  "name" string NOT NULL,
  "mimeType" string NOT NULL,
  "fileSize" bigint NOT NULL,
  "ownerId" uuid NOT NULL,
  "folderId" uuid,
  "folderPath" string NOT NULL,
  "createdAt" datetime NOT NULL,
  "updatedAt" datetime NOT NULL
);

CREATE TABLE "filesystem"."permission" (
  "id" uuid PRIMARY KEY NOT NULL,
  "fileId" uuid NOT NULL,
  "owner_id" uuid NOT NULL,
  "isPublic" boolean NOT NULL DEFAULT false,
  "shareWithId" uuid,
  "canView" boolean NOT NULL DEFAULT false,
  "canEdit" boolean NOT NULL DEFAULT false,
  "createdAt" datetime NOT NULL,
  "updatedAt" datetime NOT NULL
);

CREATE TABLE "filesystem"."metadata" (
  "id" uuid PRIMARY KEY NOT NULL,
  "fileId" uuid NOT NULL,
  "aiTag" string[],
  "summary" string,
  "sensitiveFlag" boolean NOT NULL DEFAULT false,
  "properties" JSON,
  "createdAt" datetime NOT NULL,
  "updatedAt" datetime NOT NULL
);

CREATE TABLE "accounts"."user" (
  "id" uuid PRIMARY KEY NOT NULL,
  "profilePic" "String",
  "firstName" string NOT NULL,
  "lastName" string NOT NULL,
  "email" string UNIQUE NOT NULL,
  "password" string NOT NULL,
  "createdAt" datetime NOT NULL,
  "updatedAt" datetime NOT NULL
);

CREATE TABLE "jobs"."details" (
  "id" uuid PRIMARY KEY NOT NULL,
  "file_id" uuid NOT NULL,
  "job_type" "jobs"."job_type" NOT NULL,
  "job_status" "jobs"."job_status" NOT NULL,
  "result" string,
  "createdAt" datetime NOT NULL,
  "updatedAt" datetime NOT NULL
);

ALTER TABLE "filesystem"."folder" ADD FOREIGN KEY ("parentFolder") REFERENCES "filesystem"."folder" ("id");

ALTER TABLE "filesystem"."file" ADD FOREIGN KEY ("folderId") REFERENCES "filesystem"."folder" ("id");

ALTER TABLE "filesystem"."permission" ADD FOREIGN KEY ("fileId") REFERENCES "filesystem"."file" ("id");

ALTER TABLE "filesystem"."metadata" ADD FOREIGN KEY ("fileId") REFERENCES "filesystem"."file" ("id");

ALTER TABLE "jobs"."details" ADD FOREIGN KEY ("file_id") REFERENCES "filesystem"."file" ("id");
